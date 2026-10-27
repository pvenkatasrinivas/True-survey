package com.truesurvey.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for survey CRUD: the whole app runs against an in-memory H2 database.
 * @Transactional rolls back the database after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SurveyControllerTest {

    static final String CANTEEN_SURVEY = "{"
            + "\"title\":\"Canteen Feedback\","
            + "\"description\":\"Tell us what you think\","
            + "\"questions\":["
            + "{\"text\":\"Your name\",\"type\":\"SHORT_TEXT\",\"required\":false,\"options\":[]},"
            + "{\"text\":\"How was the food?\",\"type\":\"SINGLE_CHOICE\",\"required\":true,\"options\":[\"Good\",\"Average\",\"Poor\"]},"
            + "{\"text\":\"Which meals do you eat here?\",\"type\":\"MULTIPLE_CHOICE\",\"required\":true,\"options\":[\"Breakfast\",\"Lunch\",\"Dinner\"]}"
            + "]}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetSurveyWithQuestionsInOrder() throws Exception {
        long id = createSurvey(CANTEEN_SURVEY);

        mockMvc.perform(get("/api/surveys/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Canteen Feedback"))
                .andExpect(jsonPath("$.responseCount").value(0))
                .andExpect(jsonPath("$.questionsLocked").value(false))
                .andExpect(jsonPath("$.questions", hasSize(3)))
                .andExpect(jsonPath("$.questions[0].type").value("SHORT_TEXT"))
                .andExpect(jsonPath("$.questions[0].position").value(1))
                .andExpect(jsonPath("$.questions[1].text").value("How was the food?"))
                .andExpect(jsonPath("$.questions[1].options", hasSize(3)))
                .andExpect(jsonPath("$.questions[1].options[0].text").value("Good"))
                .andExpect(jsonPath("$.questions[1].options[0].id", notNullValue()))
                .andExpect(jsonPath("$.questions[2].required").value(true));
    }

    @Test
    void listAndSearchSurveys() throws Exception {
        createSurvey(CANTEEN_SURVEY);
        createSurvey(CANTEEN_SURVEY.replace("Canteen Feedback", "Training Session Review"));

        mockMvc.perform(get("/api/surveys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].questionCount").value(3))
                .andExpect(jsonPath("$[0].responseCount").value(0));

        mockMvc.perform(get("/api/surveys").param("search", "canteen"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Canteen Feedback"));
    }

    @Test
    void updateReplacesQuestionsWhenThereAreNoResponses() throws Exception {
        long id = createSurvey(CANTEEN_SURVEY);
        String updated = "{\"title\":\"Canteen Feedback 2026\",\"description\":\"\","
                + "\"questions\":[{\"text\":\"Any suggestions?\",\"type\":\"SHORT_TEXT\",\"required\":true}]}";

        mockMvc.perform(put("/api/surveys/" + id).contentType(MediaType.APPLICATION_JSON).content(updated))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Canteen Feedback 2026"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.questions", hasSize(1)))
                .andExpect(jsonPath("$.questions[0].text").value("Any suggestions?"));
    }

    @Test
    void deleteSurvey() throws Exception {
        long id = createSurvey(CANTEEN_SURVEY);

        mockMvc.perform(delete("/api/surveys/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/surveys/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Survey not found with id " + id));
    }

    @Test
    void invalidSurveyReturnsFieldErrors() throws Exception {
        String invalid = "{\"title\":\"ab\",\"questions\":[]}";

        mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must be between 3 and 150 characters"))
                .andExpect(jsonPath("$.fieldErrors.questions").value("A survey needs between 1 and 50 questions"));
    }

    @Test
    void choiceQuestionNeedsTwoUniqueOptions() throws Exception {
        String oneOption = "{\"title\":\"Quick poll\",\"questions\":["
                + "{\"text\":\"Pick one\",\"type\":\"SINGLE_CHOICE\",\"required\":true,\"options\":[\"Yes\"]}]}";
        mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON).content(oneOption))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['questions[0].options']")
                        .value("Choice questions need between 2 and 10 options"));

        String duplicates = "{\"title\":\"Quick poll\",\"questions\":["
                + "{\"text\":\"Pick one\",\"type\":\"SINGLE_CHOICE\",\"required\":true,\"options\":[\"Yes\",\"yes\"]}]}";
        mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON).content(duplicates))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['questions[0].options']").value("Options must be unique"));
    }

    @Test
    void blankQuestionTextIsRejected() throws Exception {
        String blankText = "{\"title\":\"Quick poll\",\"questions\":[{\"text\":\"\",\"type\":\"SHORT_TEXT\"}]}";
        mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON).content(blankText))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['questions[0].text']", notNullValue()));
    }

    @Test
    void unknownQuestionTypeIsRejected() throws Exception {
        String badType = "{\"title\":\"Quick poll\",\"questions\":[{\"text\":\"Pick one\",\"type\":\"DROPDOWN\"}]}";
        mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON).content(badType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("SHORT_TEXT")));
    }

    @Test
    void unknownOrInvalidIdIsHandled() throws Exception {
        mockMvc.perform(get("/api/surveys/99999")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/surveys/99999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/surveys/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void swaggerDocumentationIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("True Survey API")))
                .andExpect(content().string(containsString("/api/surveys/{surveyId}/responses")));
    }

    long createSurvey(String json) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/surveys").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }
}
