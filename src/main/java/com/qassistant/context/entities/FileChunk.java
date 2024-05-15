package com.qassistant.context.entities;

import java.util.List;

public record FileChunk(String projectId, String checksum, String filePath, List<String> sections) {
}
