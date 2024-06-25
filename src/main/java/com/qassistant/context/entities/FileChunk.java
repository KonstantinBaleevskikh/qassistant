package com.qassistant.context.entities;


import java.util.List;
import java.util.Objects;

/**
 * Represents a chunk of a file including its metadata like checksum and path, along with the contents divided into sections.
 */
public final class FileChunk {
    private final String projectId;
    private final String checksum;
    private final String filePath;
    private final List<String> sections;

    /**
     * Constructs a new FileChunk with project identification, file metadata, and content sections.
     *
     * @param projectId the identifier of the project this file chunk belongs to
     * @param checksum the checksum of the file for integrity verification
     * @param filePath the path of the file within the project's file system
     * @param sections the content of the file split into manageable sections
     */
    public FileChunk(String projectId, String checksum, String filePath, List<String> sections) {
        this.projectId = projectId;
        this.checksum = checksum;
        this.filePath = filePath;
        this.sections = sections;
    }

    /**
     * Provides a string representation of this file chunk including all its metadata and number of sections.
     */
    @Override
    public String toString() {
        return "FileChunk{" +
                "projectId='" + projectId + '\'' +
                ", checksum='" + checksum + '\'' +
                ", filePath='" + filePath + '\'' +
                ", sections size=" + (sections != null ? sections.size() : 0) +
                '}';
    }

    /**
     * Generates a hash code for this file chunk based on its projectId, checksum, filePath, and sections.
     */
    @Override
    public int hashCode() {
        return Objects.hash(projectId, checksum, filePath, sections);
    }

    /**
     * Compares this file chunk to another object to determine equality.
     * Two file chunks are considered equal if they have the same projectId, checksum, filePath, and sections.
     *
     * @param o the object to compare with this file chunk
     * @return true if the provided object represents a file chunk equivalent to this file chunk, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileChunk fileChunk = (FileChunk) o;
        return Objects.equals(projectId, fileChunk.projectId) &&
                Objects.equals(checksum, fileChunk.checksum) &&
                Objects.equals(filePath, fileChunk.filePath) &&
                Objects.equals(sections, fileChunk.sections);
    }

    // Getters
    public String projectId() {
        return projectId;
    }

    public String checksum() {
        return checksum;
    }

    public String filePath() {
        return filePath;
    }

    public List<String> sections() {
        return sections;
    }
}
