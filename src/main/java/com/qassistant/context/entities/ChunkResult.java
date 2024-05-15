package com.qassistant.context.entities;

import java.util.List;

public record ChunkResult(List<FileChunk> fileChunks, int indexed, int skipped) {
}
