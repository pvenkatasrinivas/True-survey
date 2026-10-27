package com.truesurvey.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for submitting answers, listing responses and the summary. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ids of the canteen survey created before each test
    private long surveyId;
    private long nameQ;
    private long foodQ;
    private long mealsQ;
    private long good;
    private long average;
    private long breakfast;
    private long lunch;
    private long dinner;

    @BeforeEach
    void createCanteenSurvey() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON)
                        .content(SurveyControllerTest.CANTEEN_SURVEY))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode survey = objectMapper.readTree(result.getResponse().getContentAsString());
        surveyId = survey.get("id").asLong();
        JsonNode questions = survey.get("questions");
        nameQ = questions.get(0).get("id").asLong();
        foodQ = questions.get(1).get("id").asLong();
        mealsQ = questions.get(2).get("id").asLong();
        good = questions.get(1).get("options").get(0).get("id").asLong();
        average = questions.get(1).get("options").get(1).get("id").asLong();
        breakfast = questions.get(2).get("options").get(0).get("id").asLong();
        lunch = questions.get(2).get("options").get(1).get("id").asLong();
        dinner = questions.get(2).get("options").get(2).get("id").asLong();
    }

    @Test
    void submitValidResponse() throws Exception {
        submit("{\"answers\":["
                + "{\"questionId\":" + nameQ + ",\"text\":\"  Ravi  \"},"
                + "{\"questionId\":" + foodQ + ",\"optionIds\":[" + good + "]},"
                + "{\"questionId\":" + mealsQ + ",\"optionIds\":[" + breakfast + "," + lunch + "]}]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Thank you! Your response has been submitted."));

        mockMvc.perform(get("/api/surveys/" + surveyId + "/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].answers", hasSize(3)))
                .andExpect(jsonPath("$[0].answers[0].answer").value("Ravi"))
                .andExpect(jsonPath("$[0].answers[1].answer").value("Good"))
                .andExpect(jsonPath("$[0].answers[2].answer").value("Breakfast, Lunch"));
    }

    @Test
    void optionalQuestionCanBeSkipped() throws Exception {
        submit("{\"answers\":["
                + "{\"questionId\":" + foodQ + ",\"optionIds\":[" + average + "]},"
                + "{\"questionId\":" + mealsQ + ",\"optionIds\":[" + dinner + "]}]}")
                .andExpect(status().isCreated());
    }

    @Test
    void missingRequiredAnswersAreReportedPerQuestion() throws Exception {
        submit("{\"answers\":[{\"questionId\":" + nameQ + ",\"text\":\"Ravi\"}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please fix the highlighted answers"))
                .andExpect(jsonPath("$.fieldErrors['answers." + foodQ + "']").value("This question is required"))
                .andExpect(jsonPath("$.fieldErrors['answers." + mealsQ + "']").value("This question is required"));

        // nothing was saved
        mockMvc.perform(get("/api/surveys/" + surveyId + "/responses")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void invalidAnswersAreRejected() throws Exception {
        // two options for a single-choice question
        submit("{\"answers\":["
                + "{\"questionId\":" + foodQ + ",\"optionIds\":[" + good + "," + average + "]},"
                + "{\"questionId\":" + mealsQ + ",\"optionIds\":[" + breakfast + "]}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['answers." + foodQ + "']").value("Please choose only one option"));

        // option that belongs to another question
        submit("{\"answers\":["
                + "{\"questionId\":" + foodQ + ",\"optionIds\":[" + breakfast + "]},"
                + "{\"questionId\":" + mealsQ + ",\"optionIds\":[" + lunch + "]}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['answers." + foodQ + "']")
                        .value("Option " + breakfast + " is not valid for this question"));

        // question that is not part of this survey
        submit("{\"answers\":[{\"questionId\":999999,\"text\":\"hello\"}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['answers.999999']")
                        .value("Question 999999 does not belong to this survey"));
    }

    @Test
    void submittingToUnknownSurveyReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/surveys/999999/responses").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void summaryCountsAndPercentages() throws Exception {
        submit(answers("Ravi", good, breakfast, lunch)).andExpect(status().isCreated());
        submit(answers(null, good, lunch)).andExpect(status().isCreated());
        submit(answers("Priya", average, lunch, dinner)).andExpect(status().isCreated());
        submit(answers(null, good, breakfast)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/surveys/" + surveyId + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResponses").value(4))
                // name question: 2 of 4 answered, newest first
                .andExpect(jsonPath("$.questions[0].answeredCount").value(2))
                .andExpect(jsonPath("$.questions[0].textAnswers[0]").value("Priya"))
                .andExpect(jsonPath("$.questions[0].textAnswers[1]").value("Ravi"))
                // food: Good 3/4 = 75%, Average 1/4 = 25%, Poor 0
                .andExpect(jsonPath("$.questions[1].answeredCount").value(4))
                .andExpect(jsonPath("$.questions[1].options[0].count").value(3))
                .andExpect(jsonPath("$.questions[1].options[0].percentage").value(75.0))
                .andExpect(jsonPath("$.questions[1].options[1].percentage").value(25.0))
                .andExpect(jsonPath("$.questions[1].options[2].count").value(0))
                .andExpect(jsonPath("$.questions[1].options[2].percentage").value(0.0))
                // meals (multiple choice): Lunch picked by 3 of 4 people
                .andExpect(jsonPath("$.questions[2].options[1].count").value(3))
                .andExpect(jsonPath("$.questions[2].options[1].percentage").value(75.0))
                .andExpect(jsonPath("$.questions[2].options[2].percentage").value(25.0));

        mockMvc.perform(get("/api/surveys/" + surveyId))
                .andExpect(jsonPath("$.responseCount").value(4))
                .andExpect(jsonPath("$.questionsLocked").value(true));
    }

    @Test
    void summaryOfSurveyWithoutResponsesIsAllZero() throws Exception {
        mockMvc.perform(get("/api/surveys/" + surveyId + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResponses").value(0))
                .andExpect(jsonPath("$.questions[1].answeredCount").value(0))
                .andExpect(jsonPath("$.questions[1].options[0].percentage").value(0.0));
    }

    @Test
    void questionsAreLockedOnceThereAreResponses() throws Exception {
        submit(answers(null, good, lunch)).andExpect(status().isCreated());

        String update = "{\"title\":\"Canteen Feedback (Nov)\","
                + "\"questions\":[{\"text\":\"Completely new question\",\"type\":\"SHORT_TEXT\"}]}";
        mockMvc.perform(put("/api/surveys/" + surveyId).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Canteen Feedback (Nov)"))
                .andExpect(jsonPath("$.questions", hasSize(3)))
                .andExpect(jsonPath("$.questions[1].text").value("How was the food?"));
    }

    @Test
    void deletingSurveyAlsoDeletesResponses() throws Exception {
        submit(answers("Ravi", good, lunch)).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/surveys/" + surveyId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/surveys/" + surveyId + "/responses")).andExpect(status().isNotFound());
    }

    // ---------- helpers ----------

    private org.springframework.test.web.servlet.ResultActions submit(String json) throws Exception {
        return mockMvc.perform(post("/api/surveys/" + surveyId + "/responses")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Builds a submission: optional name, one food option, one or more meal options. */
    private String answers(String name, long foodOption, long... mealOptions) {
        StringBuilder json = new StringBuilder("{\"answers\":[");
        if (name != null) {
            json.append("{\"questionId\":").append(nameQ).append(",\"text\":\"").append(name).append("\"},");
        }
        json.append("{\"questionId\":").append(foodQ).append(",\"optionIds\":[").append(foodOption).append("]},");
        json.append("{\"questionId\":").append(mealsQ).append(",\"optionIds\":[");
        for (int i = 0; i < mealOptions.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(mealOptions[i]);
        }
        json.append("]}]}");
        return json.toString();
    }
}
