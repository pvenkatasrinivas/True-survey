package com.truesurvey.controller;

import com.truesurvey.dto.ResponseViewDto;
import com.truesurvey.dto.SubmitResponseRequest;
import com.truesurvey.dto.SubmitResponseResult;
import com.truesurvey.dto.SurveyResultSummaryDto;
import com.truesurvey.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/** Endpoints for answering a survey and looking at the collected responses. */
@RestController
@RequestMapping("/api/surveys/{surveyId}")
@Tag(name = "Responses", description = "Submit answers and view responses and the summary")
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @PostMapping("/responses")
    @Operation(summary = "Submit answers to a survey",
            description = "Use 'text' for SHORT_TEXT and 'optionIds' for choice questions. "
                    + "Errors are returned per question as fieldErrors['answers.<questionId>'].")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Response saved"),
            @ApiResponse(responseCode = "400", description = "Missing required answers or invalid answers"),
            @ApiResponse(responseCode = "404", description = "Survey not found")
    })
    public ResponseEntity<SubmitResponseResult> submit(@PathVariable Long surveyId,
                                                       @Valid @RequestBody SubmitResponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responseService.submit(surveyId, request));
    }

    @GetMapping("/responses")
    @Operation(summary = "List all responses of a survey (newest first)")
    public List<ResponseViewDto> getResponses(@PathVariable Long surveyId) {
        return responseService.getResponses(surveyId);
    }

    @GetMapping("/summary")
    @Operation(summary = "Summary: response count, option counts and percentages, latest text answers")
    public SurveyResultSummaryDto getSummary(@PathVariable Long surveyId) {
        return responseService.getSummary(surveyId);
    }
}
