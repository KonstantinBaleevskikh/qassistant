package com.qassistant.context.services.context;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Base class for services that process textual data for different contexts.
 * Ensures operation only when specific application properties are set.
 */
@ConditionalOnProperty(prefix = "application.gpt", name = {"embeddingsModel"})
public abstract class AbstractContextService {

    /**
     * Splits a large string into chunks that are easier to process,
     * ensuring each chunk does not exceed a specified maximum length.
     *
     * @param text The entire string to be split.
     * @param maxChunkSize The maximum length of each chunk.
     * @return A list of string chunks, each of a specified maximum length.
     */
    protected List<String> splitTextIntoChunks(String text, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;

        // Split the text by lines to ensure logical separations
        String[] lines = text.split("\n");

        for (String line : lines) {
            // If the line itself exceeds the maximum chunk size
            if (line.length() > maxChunkSize) {
                // If there's accumulated text in the current chunk, store it first
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                    currentLength = 0;
                }

                // Split long line into the necessary number of chunks
                while (!line.isEmpty()) {
                    int cutPoint = Math.min(maxChunkSize, line.length());
                    chunks.add(line.substring(0, cutPoint));
                    line = line.substring(cutPoint);
                }
                continue;
            }

            // If adding this line would exceed the chunk size, store the current chunk first
            if (currentLength + line.length() + 1 > maxChunkSize) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
                currentLength = 0;
            }

            // Append this line to the current chunk and update length
            currentChunk.append(line).append("\n");
            currentLength += line.length() + 1;  // Include newline character in count
        }

        // Add the final chunk if anything is left over
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }
}
