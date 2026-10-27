package com.truesurvey.service;

import com.truesurvey.dto.AnswerRequest;
import com.truesurvey.dto.ResponseViewDto;
import com.truesurvey.dto.SubmitResponseRequest;
import com.truesurvey.dto.SubmitResponseResult;
import com.truesurvey.dto.SurveyResultSummaryDto;
import com.truesurvey.entity.Answer;
import com.truesurvey.entity.Question;
import com.truesurvey.entity.QuestionOption;
import com.truesurvey.entity.QuestionType;
import com.truesurvey.entity.Survey;
import com.truesurvey.entity.SurveyResponse;
import com.truesurvey.exception.ValidationException;
import com.truesurvey.repository.AnswerRepository;
import com.truesurvey.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Submitting answers, listing responses and building the result summary. */
@Service
public class ResponseService {

    static final int MAX_TEXT_LENGTH = 1000;
    static final int MAX_TEXT_ANSWERS_IN_SUMMARY = 20;

    private final SurveyService surveyService;
    private final SurveyResponseRepository responseRepository;
    private final AnswerRepository answerRepository;

    public ResponseService(SurveyService surveyService, SurveyResponseRepository responseRepository,
                           AnswerRepository answerRepository) {
        this.surveyService = surveyService;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
    }

    // =====================================================================
    // Submit
    // =====================================================================

    /**
     * Validates every answer against its question, then saves one SurveyResponse with its Answers.
     * All problems are collected and returned together, keyed "answers.<questionId>".
     */
    @Transactional
    public SubmitResponseResult submit(Long surveyId, SubmitResponseRequest request) {
        Survey survey = surveyService.findSurvey(surveyId);

        // questionId -> Question, for quick lookup
        Map<Long, Question> questionsById = new LinkedHashMap<Long, Question>();
        for (Question question : survey.getQuestions()) {
            questionsById.put(question.getId(), question);
        }

        List<AnswerRequest> answers = request.getAnswers() == null
                ? new ArrayList<AnswerRequest>() : request.getAnswers();
        Map<String, String> errors = new LinkedHashMap<String, String>();
        Map<Long, AnswerRequest> answersByQuestion = new HashMap<Long, AnswerRequest>();

        // 1. Every answer must belong to a question of this survey, once.
        for (AnswerRequest answer : answers) {
            Long questionId = answer.getQuestionId();
            if (!questionsById.containsKey(questionId)) {
                errors.put("answers." + questionId, "Question " + questionId + " does not belong to this survey");
            } else if (answersByQuestion.containsKey(questionId)) {
                errors.put("answers." + questionId, "This question was answered more than once");
            } else {
                answersByQuestion.put(questionId, answer);
            }
        }

        // 2. Check each question (required + type rules) and build the Answer rows.
        SurveyResponse response = new SurveyResponse(survey);
        for (Question question : questionsById.values()) {
            String key = "answers." + question.getId();
            if (errors.containsKey(key)) {
                continue;
            }
            String error = addAnswersForQuestion(question, answersByQuestion.get(question.getId()), response);
            if (error != null) {
                errors.put(key, error);
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Please fix the highlighted answers", errors);
        }

        SurveyResponse saved = responseRepository.save(response); // cascade saves the answers
        return new SubmitResponseResult(saved.getId(), "Thank you! Your response has been submitted.");
    }

    /**
     * Validates one question's answer and, if valid, adds the Answer rows to the response.
     * @return an error message, or null when everything is fine
     */
    private String addAnswersForQuestion(Question question, AnswerRequest answer, SurveyResponse response) {
        if (question.getType() == QuestionType.SHORT_TEXT) {
            String text = (answer == null || answer.getText() == null) ? "" : answer.getText().trim();
            if (text.isEmpty()) {
                return question.isRequired() ? "This question is required" : null; // optional + empty = skipped
            }
            if (text.length() > MAX_TEXT_LENGTH) {
                return "Answer can be at most " + MAX_TEXT_LENGTH + " characters";
            }
            response.addAnswer(Answer.text(question, text));
            return null;
        }

        // Choice questions: remove duplicate ids, keep the order
        Set<Long> selectedIds = new LinkedHashSet<Long>();
        if (answer != null && answer.getOptionIds() != null) {
            for (Long optionId : answer.getOptionIds()) {
                if (optionId != null) {
                    selectedIds.add(optionId);
                }
            }
        }
        if (selectedIds.isEmpty()) {
            return question.isRequired() ? "This question is required" : null;
        }
        if (question.getType() == QuestionType.SINGLE_CHOICE && selectedIds.size() > 1) {
            return "Please choose only one option";
        }

        Map<Long, QuestionOption> optionsById = new HashMap<Long, QuestionOption>();
        for (QuestionOption option : question.getOptions()) {
            optionsById.put(option.getId(), option);
        }
        List<Answer> rows = new ArrayList<Answer>();
        for (Long optionId : selectedIds) {
            QuestionOption option = optionsById.get(optionId);
            if (option == null) {
                return "Option " + optionId + " is not valid for this question";
            }
            rows.add(Answer.choice(question, option));
        }
        for (Answer row : rows) {
            response.addAnswer(row);
        }
        return null;
    }

    // =====================================================================
    // List responses
    // =====================================================================

    @Transactional(readOnly = true)
    public List<ResponseViewDto> getResponses(Long surveyId) {
        Survey survey = surveyService.findSurvey(surveyId);
        List<SurveyResponse> responses = responseRepository.findWithAnswersBySurveyId(surveyId);

        List<ResponseViewDto> result = new ArrayList<ResponseViewDto>();
        for (SurveyResponse response : responses) {
            List<ResponseViewDto.AnswerView> answerViews = new ArrayList<ResponseViewDto.AnswerView>();
            // Walk the questions in survey order and show what was answered for each
            for (Question question : survey.getQuestions()) {
                String answerText = answerTextFor(question, response.getAnswers());
                if (answerText != null) {
                    answerViews.add(new ResponseViewDto.AnswerView(question.getId(), question.getText(), answerText));
                }
            }
            result.add(new ResponseViewDto(response.getId(), response.getSubmittedAt(), answerViews));
        }
        return result;
    }

    /** The answer to one question as display text, or null if it was not answered. */
    private String answerTextFor(Question question, List<Answer> answers) {
        if (question.getType() == QuestionType.SHORT_TEXT) {
            for (Answer answer : answers) {
                if (answer.getQuestion().getId().equals(question.getId())) {
                    return answer.getTextValue();
                }
            }
            return null;
        }
        // Choice: list the selected options in the order they appear in the question
        Set<Long> selected = new HashSet<Long>();
        for (Answer answer : answers) {
            if (answer.getQuestion().getId().equals(question.getId()) && answer.getOption() != null) {
                selected.add(answer.getOption().getId());
            }
        }
        if (selected.isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (QuestionOption option : question.getOptions()) {
            if (selected.contains(option.getId())) {
                if (text.length() > 0) {
                    text.append(", ");
                }
                text.append(option.getText());
            }
        }
        return text.toString();
    }

    // =====================================================================
    // Summary
    // =====================================================================

    @Transactional(readOnly = true)
    public SurveyResultSummaryDto getSummary(Long surveyId) {
        Survey survey = surveyService.findSurvey(surveyId);
        long totalResponses = responseRepository.countBySurvey_Id(surveyId);
        List<Answer> allAnswers = answerRepository.findAllBySurveyId(surveyId); // newest response first

        SurveyResultSummaryDto summary = new SurveyResultSummaryDto(survey.getId(), survey.getTitle(), totalResponses);

        for (Question question : survey.getQuestions()) {
            Set<Long> respondents = new HashSet<Long>();          // responses that answered this question
            Map<Long, Long> optionCounts = new HashMap<Long, Long>(); // optionId -> how many picked it
            List<String> texts = new ArrayList<String>();

            for (Answer answer : allAnswers) {
                if (!answer.getQuestion().getId().equals(question.getId())) {
                    continue;
                }
                respondents.add(answer.getResponse().getId());
                if (answer.getOption() != null) {
                    Long optionId = answer.getOption().getId();
                    Long current = optionCounts.get(optionId);
                    optionCounts.put(optionId, current == null ? 1L : current + 1L);
                } else if (answer.getTextValue() != null && texts.size() < MAX_TEXT_ANSWERS_IN_SUMMARY) {
                    texts.add(answer.getTextValue());
                }
            }

            long answeredCount = respondents.size();
            SurveyResultSummaryDto.QuestionSummary qs = new SurveyResultSummaryDto.QuestionSummary(
                    question.getId(), question.getText(), question.getType(), answeredCount);
            for (QuestionOption option : question.getOptions()) {
                Long count = optionCounts.get(option.getId());
                long c = count == null ? 0L : count;
                qs.getOptions().add(new SurveyResultSummaryDto.OptionCount(
                        option.getId(), option.getText(), c, percentage(c, answeredCount)));
            }
            qs.getTextAnswers().addAll(texts);
            summary.getQuestions().add(qs);
        }
        return summary;
    }

    /** count / total * 100 rounded to 1 decimal; 0 when total is 0 (no division by zero). */
    static double percentage(long count, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(count * 1000.0 / total) / 10.0;
    }
}
