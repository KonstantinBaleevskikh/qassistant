package com.qassistant.context.entities;

import java.util.List;
import java.util.Objects;

/**
 * Represents the result of an operation that processes chunks of files,
 * capturing details about how many chunks were processed and how many were skipped.
 */
public final class ChunkResult {
    private final String projectId;
    private final List<FileChunk> fileChunks;
    private final int indexed;
    private final int skipped;

    public ChunkResult(String projectId, List<FileChunk> fileChunks, int indexed, int skipped) {
        this.projectId = projectId;
        this.fileChunks = fileChunks;
        this.indexed = indexed;
        this.skipped = skipped;
    }

    /**
     * Returns a string representation of the object.
     * @return A string that represents the object.
     */
    @Override
    public String toString() {
        return "ChunkResult{" +
                "projectId='" + projectId + '\'' +
                ", fileChunks=" + fileChunks +
                ", indexed=" + indexed +
                ", skipped=" + skipped +
                '}';
    }

    /**
     * Returns a hash code value for the object.
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(projectId, fileChunks, indexed, skipped);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkResult that = (ChunkResult) o;
        return indexed == that.indexed &&
                skipped == that.skipped &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(fileChunks, that.fileChunks);
    }

    // Getter methods for accessing properties
    public String projectId() {
        return projectId;
    }

    public List<FileChunk> fileChunks() {
        return fileChunks;
    }

    public int indexed() {
        return indexed;
    }

    public int skipped() {
        return skipped;
    }
}