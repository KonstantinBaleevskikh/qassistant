package com.qassistant.context.services.context;

import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.entities.FileChunk;
import com.qassistant.context.configs.ContextConfig;
import com.qassistant.context.configs.ContextGithubConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "application", name = {"github.token", "github.userName"})
@ConditionalOnBean(DbService.class)
public class GithubContextService extends AbstractContextService {
    private final Logger logger = LoggerFactory.getLogger(GithubContextService.class);
    private final DbService dbService;
    private final ContextGithubConfig githubConfig;
    private final ContextConfig contextConfig;

    public GithubContextService(DbService dbService, ContextConfig contextConfig, ContextGithubConfig githubConfig) {
        this.dbService = dbService;
        this.githubConfig = githubConfig;
        this.contextConfig = contextConfig;
    }

    /**
     * Retrieves and indexes file contents from a GitHub repository for a specified project.
     *
     * @param projectId The project ID.
     * @param repoName The repository name.
     * @param path The path within the repository.
     * @return List of indexed strings.
     */
    public ChunkResult fetchAndIndexFiles(String projectId, String repoName, String path) {
        Project project = dbService.findProjectById(projectId)
                .orElseGet(() -> dbService.findProjectByName(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found: " + projectId)));

        try {
            GitHub gitHub = GitHub.connect(githubConfig.getUserName(), githubConfig.getToken());
            GHRepository repository = gitHub.getRepository(repoName);
            return indexRepositoryContents(project.getId(), repository, path, new ArrayList<>());
        } catch (Exception e) {
            logger.error("Error processing GitHub repository: {}", repoName, e);
            throw new RuntimeException("Error processing GitHub repository", e);
        }
    }

    /**
     * Recursively indexes contents of a GitHub repository.
     *
     * @param projectId Project ID for referencing in chunks.
     * @param repository GitHub repository object.
     * @param path Current repository path to explore.
     * @param chunks Accumulated list of FileChunks.
     * @return Result of indexing including statistics.
     */
    private ChunkResult indexRepositoryContents(String projectId, GHRepository repository, String path, List<FileChunk> chunks) {
        if (chunks == null) {
            chunks = new ArrayList<>();
        }
        int indexedCount = 0;
        int skippedCount = 0;

        try {
            List<GHContent> contents = repository.getDirectoryContent(path);
            for (GHContent content : contents) {
                if (content.isFile() && !shouldIgnoreFile(content.getName())) {
                    try {
                        String fileContent = IOUtils.toString(content.read(), StandardCharsets.UTF_8);
                        List<String> tokens = this.splitTextIntoChunks(fileContent, contextConfig.getSourceMaxToken());
                        if (!tokens.isEmpty()) {
                            chunks.add(new FileChunk(projectId, content.getSha(), content.getPath(), tokens));
                            indexedCount++;
                        }
                    } catch (Exception e) {
                        logger.error("Error reading file: {}", content.getPath(), e);
                        skippedCount++;
                    }
                } else if (content.isDirectory()) {
                    ChunkResult result = indexRepositoryContents(projectId, repository, content.getPath(), chunks);
                    indexedCount += result.indexed();
                    skippedCount += result.skipped();
                }
            }

            return new ChunkResult(projectId, chunks, indexedCount, skippedCount);
        } catch (IOException e) {
            throw new RuntimeException("Failed to access repository contents", e);
        }
    }

    /**
     * Determines if a file should be ignored based on its name.
     *
     * @param fileName The name of the file.
     * @return true if the file should be ignored, false otherwise.
     */
    private boolean shouldIgnoreFile(String fileName) {
        return contextConfig.getIgnorePatterns().stream().anyMatch(fileName::endsWith);
    }
}
