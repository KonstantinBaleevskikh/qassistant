package com.qassistant.context.db.service;

import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.entities.Context;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface DbService {
    @Transactional
    public Project saveProject(Project var1);

    @Transactional
    public void deleteProject(String var1);

    public Optional<Project> findProjectById(String var1);

    public Optional<Project> findProjectByName(String var1);

    public Iterable<Project> findAllProjects();

    public void deleteAllFilesByProject(String var1);

    public void deleteFileByProjectAndName(String var1, String var2);

    public int countFilesByProject(String var1);

    public double setWeightForFile(String var1, String var2, double var3);

    public double setWeightForSections(String var1, List<String> var2, double var3);

    public Map<String, String> indexClassification(String var1, Map<String, String> var2, String var3);

    public List<String> indexChunkResult(ChunkResult var1);

    public List<Context> findContext(String var1, String var2, int var3);
}
