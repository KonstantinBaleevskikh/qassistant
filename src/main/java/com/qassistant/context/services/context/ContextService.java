package com.qassistant.context.services.context;

import com.qassistant.context.entities.ChunkResult;

import java.util.List;

public interface ContextService {
    List<String> indexChunkResult(ChunkResult chunkResult);
}
