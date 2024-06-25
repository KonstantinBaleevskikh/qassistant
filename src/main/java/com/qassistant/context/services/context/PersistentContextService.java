package com.qassistant.context.services.context;

import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.ChunkResult;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service class responsible for managing context operations that require persistence.
 */
@Service
public class PersistentContextService implements ContextService {
    private final DbService dbService;

    /**
     * Constructs a PersistentContextService with a specific DbService.
     *
     * @param dbService the database service to handle context-related operations
     */
    public PersistentContextService(DbService dbService) {
        this.dbService = dbService;
    }

    /**
     * Delegates the indexing of chunk results to the DbService.
     *
     * @param chunkResult the chunk result to be indexed
     * @return a list of strings resulting from the indexing operation
     */
    @Override
    public List<String> indexChunkResult(ChunkResult chunkResult) {
        return dbService.indexChunkResult(chunkResult);
    }
}