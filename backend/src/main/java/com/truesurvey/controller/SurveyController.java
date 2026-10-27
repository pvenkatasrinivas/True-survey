package com.truesurvey.controller;

import com.truesurvey.dto.SurveyDto;
import com.truesurvey.dto.SurveyRequest;
import com.truesurvey.dto.SurveySummaryDto;
import com.truesurvey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Spring MVC REST controller for surveys. It only deals with HTTP;
 * SurveyService does the actual work. The @Operation/@ApiResponse annotations feed Swagger UI.
 */
@RestController
@RequestMapping("/api/surveys")
@Tag(name = "Surveys", description = "Create, view, edit and delete surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    @Operation(summary = "List surveys", description = "Newest first. Optional search on the title.")
    public List<SurveySummaryDto> getSurveys(
            @Parameter(description = "Text to search in survey titles") @RequestParam(required = false) String search) {
        return surveyService.getSurveys(search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one survey with its questions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Survey found"),
            @ApiResponse(responseCode = "404", description = "Survey not found")
    })
    public SurveyDto getSurvey(@PathVariable Long id) {
        return surveyService.getSurvey(id);
    }

    @PostMapping
    @Operation(summary = "Create a survey")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Survey created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<SurveyDto> createSurvey(@Valid @RequestBody SurveyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyService.createSurvey(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a survey",
            description = "If the survey already has responses, only the title and description are updated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Survey updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Survey not found")
    })
    public SurveyDto updateSurvey(@PathVariable Long id, @Valid @RequestBody SurveyRequest request) {
        return surveyService.updateSurvey(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a survey and all its responses")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Survey deleted"),
            @ApiResponse(responseCode = "404", description = "Survey not found")
    })
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return ResponseEntity.noContent().build();
    }
}
