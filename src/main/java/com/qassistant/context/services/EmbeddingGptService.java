package com.qassistant.context.services;

import com.qassistant.context.db.dbEntity.FileSection;
import com.qassistant.context.entities.FileChunk;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingGptService {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingGptService.class);
    private final OpenAiEmbeddingClient embeddingClient;

    public EmbeddingGptService(OpenAiEmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    /**
     * Attempts to get an embedding response for a given list of strings.
     *
     * @param texts List of texts to get embeddings for.
     * @return EmbeddingResponse obtained from the embedding client.
     * @throws RuntimeException if unable to get embeddings after 5 retries.
     */
    public EmbeddingResponse getEmbeddingResponse(List<String> texts) {
        int retries = 5;
        while (retries > 0) {
            try {
                return embeddingClient.embedForResponse(texts);
            } catch (Exception e) {
                logger.error("Failed to create embeddings, retries left: {}", retries - 1, e);
                retries--;
                try {
                    Thread.sleep(3000L); // 3 seconds pause before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted during retry", ex);
                }
            }
        }
        throw new RuntimeException("Could not create embeddings after multiple retries.");
    }

    /**
     * Maps each FileChunk to a set of FileSections with embeddings.
     *
     * @param fileChunks List of FileChunks to process.
     * @return Map of FileChunk to a Set of FileSections.
     */
    public Map<FileChunk, Set<FileSection>> mapChunksToSections(List<FileChunk> fileChunks) {
        Map<FileChunk, Set<FileSection>> chunkMap = new HashMap<>();
        AtomicInteger remainingChunks = new AtomicInteger(fileChunks.size());
        logger.info("Creating embeddings for {} chunks", fileChunks.size());
        ExecutorService executor = Executors.newFixedThreadPool(4);

        fileChunks.forEach(chunk -> executor.submit(() -> {
            Set<FileSection> sections = createFileSections(chunk, remainingChunks);
            synchronized (chunkMap) {
                if (!sections.isEmpty()) {
                    chunkMap.put(chunk, sections);
                }
            }
        }));

        executor.shutdown();
        try {
            while (!executor.awaitTermination(100L, TimeUnit.MILLISECONDS)) {
                logger.info("Waiting for tasks to complete... Remaining: {}", remainingChunks.get());
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for completion", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for task completion", e);
        }

        logger.info("Embeddings created and files indexed.");
        return chunkMap;
    }

    /**
     * Creates a set of FileSections from the embeddings of the given FileChunk.
     *
     * @param fileChunk The file chunk to process.
     * @param remainingCount Counter for the remaining chunks.
     * @return Set of FileSections created from the file chunk.
     */
    private Set<FileSection> createFileSections(FileChunk fileChunk, AtomicInteger remainingCount) {
        try {
            Set<FileSection> sections = new HashSet<>();
            EmbeddingResponse embeddingResponse = getEmbeddingResponse(fileChunk.sections());
            for (int i = 0; i < embeddingResponse.getResults().size(); i++) {
                sections.add(new FileSection(fileChunk.sections().get(i), embeddingResponse.getResults().get(i).getOutput(), 0));
            }
            return sections;
        } catch (Exception e) {
            logger.error("Error occurred during embedding creation and indexing for chunk: {}", fileChunk, e);
            return Collections.emptySet();
        } finally {
            remainingCount.decrementAndGet();
        }
    }
}
