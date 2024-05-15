package com.qassistant.context.db.service;

import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.entities.Context;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(value={"dev"})
public class DevDbService
implements DbService {
    @Override
    public Project saveProject(Project project) {
        return project;
    }

    @Override
    public void deleteProject(String project) {
    }

    @Override
    public Optional<Project> findProjectById(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<Project> findProjectByName(String name) {
        return Optional.empty();
    }

    @Override
    public Iterable<Project> findAllProjects() {
        return Collections.emptyList();
    }

    @Override
    public void deleteAllFilesByProject(String project) {
    }

    @Override
    public void deleteFileByProjectAndName(String project, String fileName) {
    }

    @Override
    public int countFilesByProject(String project) {
        return 0;
    }

    @Override
    public double setWeightForFile(String project, String fileName, double weight) {
        return 0.0;
    }

    @Override
    public double setWeightForSections(String project, List<String> ids, double weight) {
        return 0.0;
    }

    @Override
    public Map<String, String> indexClassification(String project, Map<String, String> promptsAndAnswers, String filePath) {
        return Map.of();
    }

    @Override
    public List<String> indexChunkResult(ChunkResult chunkResult) {
        return List.of();
    }

    @Override
    public List<Context> findContext(String project, String prompt, int contextEntries) {
        return List.of();
    }
}
