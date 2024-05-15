package com.qassistant.context.controllers;

import com.qassistant.context.db.service.DbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Context")
@RequestMapping("/context")
@ConditionalOnProperty(prefix = "application.gpt", name = "embeddingsModel")
@ConditionalOnBean(DbService.class)
public class ContextController {
    private final DbService contextDbService;

    public ContextController(DbService dbService) {
        this.contextDbService = dbService;
    }

    @Operation(summary = "Retrieve context based on project and prompt", description = "Fetches context entries for a given project and prompt.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the context")
    @PostMapping("/getContext")
    public ResponseEntity<Object> getContext(
            @Parameter(description = "Project ID for which the context is fetched") @RequestParam String projectId,
            @Parameter(description = "Prompt to query within the project") @RequestParam String prompt,
            @Parameter(description = "Number of context entries to return", required = false) @RequestParam(defaultValue = "3") int entries) {

        Object contextData = contextDbService.findContext(projectId, prompt, entries);
        return new ResponseEntity<>(contextData, HttpStatus.OK);
    }
}
