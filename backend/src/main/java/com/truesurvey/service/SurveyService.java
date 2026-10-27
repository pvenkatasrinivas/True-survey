package com.truesurvey.service;

import com.truesurvey.dto.QuestionRequest;
import com.truesurvey.dto.SurveyDto;
import com.truesurvey.dto.SurveyRequest;
import com.truesurvey.dto.SurveySummaryDto;
import com.truesurvey.entity.Question;
import com.truesurvey.entity.QuestionOption;
import com.truesurvey.entity.Survey;
import com.truesurvey.exception.ResourceNotFoundException;
import com.truesurvey.exception.ValidationException;
import com.truesurvey.repository.AnswerRepository;
import com.truesurvey.repository.SurveyRepository;
import com.truesurvey.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Business logic for creating, reading, updating and deleting surveys. */
@Service
public class SurveyService {

    static final int MIN_OPTIONS = 2;
    static final int MAX_OPTIONS = 10;
    static final int MAX_OPTION_LENGTH = 150;

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final AnswerRepository answerRepository;

    public SurveyService(SurveyRepository surveyRepository, SurveyResponseRepository responseRepository,
                         AnswerRepository answerRepository) {
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
    }

    /** All surveys (newest first), optionally only those whose title contains "search". */
    @Transactional(readOnly = true)
    public List<SurveySummaryDto> getSurveys(String search) {
        List<Survey> surveys;
        if (search == null || search.trim().isEmpty()) {
            surveys = surveyRepository.findAllByOrderByCreatedAtDescIdDesc();
        } else {
            surveys = surveyRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDescIdDesc(search.trim());
        }

        // Two GROUP BY queries instead of two queries per survey
        Map<Long, Long> questionCounts = toCountMap(surveyRepository.countQuestionsPerSurvey());
        Map<Long, Long> responseCounts = toCountMap(responseRepository.countResponsesPerSurvey());

        List<SurveySummaryDto> result = new ArrayList<SurveySummaryDto>();
        for (Survey survey : surveys) {
            result.add(new SurveySummaryDto(survey,
                    getOrZero(questionCounts, survey.getId()),
                    getOrZero(responseCounts, survey.getId())));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public SurveyDto getSurvey(Long id) {
        Survey survey = findSurvey(id);
        return SurveyDto.from(survey, responseRepository.countBySurvey_Id(id));
    }

    @Transactional
    public SurveyDto createSurvey(SurveyRequest request) {
        validateQuestions(request.getQuestions());

        Survey survey = new Survey();
        survey.setTitle(request.getTitle().trim());
        survey.setDescription(emptyToNull(request.getDescription()));
        addQuestions(survey, request.getQuestions());

        Survey saved = surveyRepository.save(survey); // cascade also saves questions and options
        return SurveyDto.from(saved, 0);
    }

    /**
     * Updates a survey. If people have already answered it, the questions are kept as they are
     * (otherwise old answers could point to questions that no longer exist) and only the
     * title and description change.
     */
    @Transactional
    public SurveyDto updateSurvey(Long id, SurveyRequest request) {
        Survey survey = findSurvey(id);
        long responseCount = responseRepository.countBySurvey_Id(id);

        survey.setTitle(request.getTitle().trim());
        survey.setDescription(emptyToNull(request.getDescription()));

        if (responseCount == 0) {
            validateQuestions(request.getQuestions());
            // clear() + add (not a new list) so orphanRemoval deletes the old questions
            survey.getQuestions().clear();
            addQuestions(survey, request.getQuestions());
            survey.touch();
        }

        Survey saved = surveyRepository.saveAndFlush(survey);
        return SurveyDto.from(saved, responseCount);
    }

    /** Deletes a survey together with all of its responses and answers. */
    @Transactional
    public void deleteSurvey(Long id) {
        if (!surveyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Survey not found with id " + id);
        }
        // Children first: answers -> responses -> survey (questions/options are removed by cascade)
        answerRepository.deleteBySurveyId(id);
        responseRepository.deleteBySurveyId(id);
        surveyRepository.deleteById(id);
    }

    /** Used by other services. */
    @Transactional(readOnly = true)
    public Survey findSurvey(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id " + id));
    }

    // ---------- helpers ----------

    private void addQuestions(Survey survey, List<QuestionRequest> requests) {
        int position = 1;
        for (QuestionRequest qr : requests) {
            Question question = new Question(qr.getText().trim(), qr.getType(), qr.isRequired(), position++);
            if (qr.getType().hasOptions()) {
                int optionPosition = 1;
                for (String optionText : qr.getOptions()) {
                    question.addOption(new QuestionOption(optionText.trim(), optionPosition++));
                }
            }
            survey.addQuestion(question);
        }
    }

    /** Rules that the @Size/@NotBlank annotations can't express. Field keys match the frontend. */
    void validateQuestions(List<QuestionRequest> questions) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        for (int i = 0; i < questions.size(); i++) {
            QuestionRequest q = questions.get(i);
            String key = "questions[" + i + "].options";
            List<String> options = q.getOptions() == null ? new ArrayList<String>() : q.getOptions();

            if (q.getType() == null) {
                continue; // already reported by @NotNull
            }
            if (!q.getType().hasOptions()) {
                if (!options.isEmpty()) {
                    errors.put(key, "Short text questions cannot have options");
                }
                continue;
            }
            if (options.size() < MIN_OPTIONS || options.size() > MAX_OPTIONS) {
                errors.put(key, "Choice questions need between " + MIN_OPTIONS + " and " + MAX_OPTIONS + " options");
                continue;
            }
            Set<String> seen = new HashSet<String>();
            for (String option : options) {
                if (option == null || option.trim().isEmpty()) {
                    errors.put(key, "Options cannot be empty");
                    break;
                }
                if (option.trim().length() > MAX_OPTION_LENGTH) {
                    errors.put(key, "Each option can be at most " + MAX_OPTION_LENGTH + " characters");
                    break;
                }
                if (!seen.add(option.trim().toLowerCase())) {
                    errors.put(key, "Options must be unique");
                    break;
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<Long, Long>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    private long getOrZero(Map<Long, Long> map, Long key) {
        Long value = map.get(key);
        return value == null ? 0L : value;
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
