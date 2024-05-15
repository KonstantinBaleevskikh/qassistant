package com.qassistant.context.controllers;

import com.qassistant.context.db.service.DbService;
import com.qassistant.context.services.PromptsClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RestController
@Tag(name = "Classification")
@RequestMapping("/classification")
@ConditionalOnProperty(prefix = "application.gpt", name = "embeddingsModel")
@ConditionalOnBean(DbService.class)
public class ClassificationController {
    private final PromptsClassificationService classificationService;
    private final DbService dbService;

    public ClassificationController(PromptsClassificationService classificationService, DbService dbService) {
        this.classificationService = classificationService;
        this.dbService = dbService;
    }

    @Operation(summary = "Create classification", description = "Create a new classification by uploading a file.")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @PostMapping(path = "/createClassification", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Object> createClassification(
            @Parameter(description = "Project identifier", required = true) @RequestParam String projectId,
            @Parameter(description = "File to classify", required = true) @RequestParam MultipartFile file) throws IOException {

        File tempFile = File.createTempFile("temp_", file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Object result = classificationService.classifyPrompts(projectId, tempFile);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Classify prompts", description = "Classify prompts in a specified project.")
    @ApiResponse(responseCode = "200", description = "Successful classification")
    @PostMapping(path = "/classify", produces = "application/json")
    public ResponseEntity<Object> classify(
            @Parameter(description = "Project identifier", required = true) @RequestParam String projectId,
            @Parameter(description = "Prompt to classify", required = true) @RequestParam String prompt,
            @Parameter(description = "Number of classification options", required = false) @RequestParam(defaultValue = "3") int options) {

        Object context = dbService.findContext(projectId, prompt, options);
        return ResponseEntity.ok(context);
    }
}
