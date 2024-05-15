package com.qassistant.context.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.prompts.NewPrompt;
import com.qassistant.context.utils.Mapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix="application.gpt", name={"embeddingsModel"})
@ConditionalOnBean(DbService.class)
public class PromptsClassificationService {
    private static final Logger logger = LoggerFactory.getLogger(PromptsClassificationService.class);
    private final DbService dbService;

    public PromptsClassificationService(DbService dbService) {
        this.dbService = dbService;
    }

    /**
     * Reads prompts from a file, deserializes them into NewPrompt objects, and indexes their messages.
     *
     * @param projectId The project identifier.
     * @param file The file containing the prompts.
     * @return A map of message contents indexed by content.
     */
    public Map<String, String> classifyPrompts(String projectId, File file) {
        // Ensure project exists
        dbService.findProjectById(projectId)
                .orElseGet(() -> dbService.findProjectByName(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found: " + projectId)));

        // Read file and process contents
        String fileContent;
        try {
            fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading file", e);
            throw new RuntimeException("Failed to read file", e);
        }

        List<NewPrompt> prompts = Arrays.stream(fileContent.split("\n"))
                .map(this::deserializeNewPrompt)
                .toList();

        Map<String, String> promptClassifications = new HashMap<>();
        for (NewPrompt prompt : prompts) {
            String key = prompt.getMessages().get(1).getContent();
            String value = prompt.getMessages().get(2).getContent();
            promptClassifications.put(key, value);
        }

        return dbService.indexClassification(projectId, promptClassifications, file.getPath());
    }

    /**
     * Helper method to deserialize JSON string into NewPrompt object.
     *
     * @param jsonLine JSON string representing a NewPrompt.
     * @return Deserialized NewPrompt object.
     */
    private NewPrompt deserializeNewPrompt(String jsonLine) {
        try {
            return Mapper.getObjectMapper().readValue(jsonLine, NewPrompt.class);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing JSON", e);
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }
}
