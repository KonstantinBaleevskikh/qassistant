package com.qassistant.context.services.context;

import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.entities.FileChunk;
import com.qassistant.context.configs.ContextConfig;
import com.qassistant.context.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(prefix="application.gpt", name={"embeddingsModel"})
@ConditionalOnBean(DbService.class)
public class FileContextService extends AbstractContextService {
    private final Logger logger = LoggerFactory.getLogger(FileContextService.class);
    private final DbService dbService;
    private final ContextConfig contextConfig;

    public FileContextService(ContextConfig contextConfig, DbService dbService) {
        this.contextConfig = contextConfig;
        this.dbService = dbService;
    }

    public List<String> findAndIndexFiles(String projectId, String directoryPath) {
        Project project = dbService.findProjectById(projectId)
                .orElseGet(() -> dbService.findProjectByName(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found: " + projectId)));

        List<String> files = listSourceFiles(directoryPath);
        ChunkResult chunkResult = indexFiles(project.getId(), files);
        return dbService.indexChunkResult(chunkResult);
    }

    private List<String> listSourceFiles(String directory) {
        List<String> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(directory))) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> !shouldIgnoreFile(path))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error finding source files in directory: {}", directory, e);
        }
        return files;
    }

    private boolean shouldIgnoreFile(Path path) {
        String pathStr = path.toString().replace(File.separator, "/");
        return contextConfig.getIgnorePatterns().stream().anyMatch(pattern ->
                (pattern.startsWith("*.") && path.getFileName().toString().endsWith(pattern.substring(1))) ||
                        (FileSystems.getDefault().getPathMatcher("glob:" + pattern).matches(path) || pathStr.contains("/" + pattern + "/"))
        );
    }

    private ChunkResult indexFiles(String projectId, List<String> files) {
        List<FileChunk> fileChunks = new ArrayList<>();
        int indexedCount = 0;
        int skippedCount = 0;

        for (String filePath : files) {
            try {
                String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
                String fileSha256 = TextUtils.generateSha256(fileContent);
                List<String> tokens = splitTextIntoChunks(fileContent, contextConfig.getSourceMaxToken());

                if (!tokens.isEmpty()) {
                    fileChunks.add(new FileChunk(projectId, fileSha256, filePath, tokens));
                    indexedCount++;
                }
            } catch (Exception e) {
                logger.error("Error reading file: {}", filePath, e);
                skippedCount++;
            }
        }

        return new ChunkResult(fileChunks, indexedCount, skippedCount);
    }
}
