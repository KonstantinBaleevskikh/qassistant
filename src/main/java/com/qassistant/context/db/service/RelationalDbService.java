package com.qassistant.context.db.service;

import com.qassistant.context.db.dbEntity.File;
import com.qassistant.context.db.dbEntity.FileSection;
import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.db.repositories.FileRepository;
import com.qassistant.context.db.repositories.FileSectionRepository;
import com.qassistant.context.db.repositories.ProjectRepository;
import com.qassistant.context.entities.Context;
import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.entities.FileChunk;
import com.qassistant.context.services.EmbeddingGptService;
import com.qassistant.context.utils.MathUtils;
import com.qassistant.context.utils.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

public class RelationalDbService implements DbService{
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationalDbService.class);
    private static final Map<String, Set<FileSection>> projectFileSectionsCache = new ConcurrentHashMap<>();
    private final FileSectionRepository fileSectionRepository;
    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final EmbeddingGptService embeddingService;

    /**
     * Constructs a new RelationalDbService with the necessary repositories and services.
     *
     * @param fileSectionRepository repository for accessing and persisting file sections
     * @param fileRepository repository for accessing and persisting files
     * @param projectRepository repository for accessing and persisting project details
     * @param embeddingService service for handling embedding operations
     */
    public RelationalDbService(FileSectionRepository fileSectionRepository, FileRepository fileRepository, ProjectRepository projectRepository, EmbeddingGptService embeddingService) {
        this.fileSectionRepository = fileSectionRepository;
        this.fileRepository = fileRepository;
        this.projectRepository = projectRepository;
        this.embeddingService = embeddingService;
    }


    @Override
    @Transactional
    public Project saveProject(Project project) {
        if (projectRepository.findByName(project.getName()).isPresent()) {
            throw new IllegalArgumentException("A project with the name '" + project.getName() + "' already exists.");
        }
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId) {
        Project existingProject = findProjectById(projectId).orElseThrow(() ->
                new RuntimeException("Project not found with ID: " + projectId));
        projectRepository.deleteById(existingProject.getId());
    }

    @Override
    public Optional<Project> findProjectById(String id) {
        return projectRepository.findById(id);
    }

    @Override
    public Optional<Project> findProjectByName(String name) {
        return projectRepository.findByName(name);
    }

    @Override
    public Iterable<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteAllFilesByProject(String projectId) {
        Project project = findProjectById(projectId).orElseThrow(() ->
                new RuntimeException("No project found with ID: " + projectId));
        int deletedCount = fileRepository.deleteFilesByProjectId(project.getId());
        if (deletedCount == 0) {
            throw new RuntimeException("No files to delete for project ID: " + projectId);
        }
    }

    @Override
    @Transactional
    public void deleteFileByProjectAndName(String projectId, String fileName) {
        Project project = findProjectById(projectId).orElseThrow(() ->
                new RuntimeException("No project found with ID: " + projectId));
        int deletedCount = fileRepository.deleteAllByProjectIdAndName(project.getId(), fileName);
        if (deletedCount == 0) {
            throw new RuntimeException("No file named '" + fileName + "' found in project ID: " + projectId);
        }
    }

    @Override
    public int countFilesByProject(String projectId) {
        Project project = findProjectById(projectId).orElseThrow(() ->
                new RuntimeException("No project found with ID: " + projectId));
        return fileRepository.countFilesByProjectId(project.getId());
    }

    @Override
    public double setWeightForFile(String projectId, String fileName, double weight) {
        // Retrieve the project or throw if not found
        Project project = findProjectById(projectId).orElseGet(() ->
                findProjectByName(projectId).orElseThrow(() ->
                        new RuntimeException("No project found with ID: " + projectId)));

        // Update the weight for all sections of the file found by projectId and fileName
        fileRepository.findByProjectIdAndName(project.getId(), fileName).ifPresent(file -> {
            file.getSections().forEach(section -> section.setWeight(weight));
            fileSectionRepository.saveAll(file.getSections());
            // Optionally clear cache after updating
            Optional.ofNullable(projectFileSectionsCache.get(project.getId())).ifPresent(Set::clear);
        });

        return weight;
    }

    @Override
    public double setWeightForSections(String projectId, List<String> ids, double weight) {
        // Ensure the project exists before proceeding
        Project project = findProjectById(projectId).orElseGet(() ->
                findProjectByName(projectId).orElseThrow(() ->
                        new RuntimeException("Project not found with ID: " + projectId)));

        // Fetch and update the weight of the file sections by their IDs
        Iterable<FileSection> fileSections = fileSectionRepository.findAllById(ids);
        fileSections.forEach(section -> section.setWeight(weight));
        fileSectionRepository.saveAll(fileSections);

        // Clear related cache if it exists
        Optional.ofNullable(projectFileSectionsCache.get(project.getId())).ifPresent(Set::clear);

        return weight;
    }


    @Override
    @Transactional
    public Map<String, String> indexClassification(String projectId, Map<String, String> promptsAndAnswers, String filePath) {
        Project project = findProjectById(projectId).orElseThrow(() ->
                new RuntimeException("No project found with ID: " + projectId));

        try {
            EmbeddingResponse response = embeddingService.getEmbeddingResponse(new ArrayList<>(promptsAndAnswers.keySet()));
            Set<FileSection> fileSections = IntStream.range(0, response.getResults().size())
                    .mapToObj(i -> new FileSection(promptsAndAnswers.values().stream().toList().get(i),
                            response.getResults().get(i).getOutput(), 0))
                    .collect(Collectors.toSet());

            String sha256Checksum = TextUtils.generateSha256(String.join(", ", promptsAndAnswers.keySet()));
            FileChunk fileChunk = new FileChunk(project.getId(), sha256Checksum, filePath, new ArrayList<>());
            associateFileChunkWithSections(fileChunk, fileSections);
        } catch (Exception e) {
            LOGGER.error("Error during embedding creation and indexing: ", e);
        }

        return promptsAndAnswers;
    }

    @Override
    @Transactional
    public List<String> indexChunkResult(ChunkResult chunkResult) {
        if (chunkResult.fileChunks().isEmpty()) {
            throw new IllegalArgumentException("fileChunks size should be more than 0");
        }
        String string = chunkResult.projectId();
        this.findProjectById(string).orElseGet(() -> this.findProjectByName(string).orElseThrow(() -> new RuntimeException("There is no such project")));
        Map<FileChunk, Set<FileSection>> map = embeddingService.mapChunksToSections(chunkResult.fileChunks());
        for (Map.Entry entry : map.entrySet()) {
            this.associateFileChunkWithSections((FileChunk)entry.getKey(), (Set)entry.getValue());
        }
        return chunkResult.fileChunks().stream().map(FileChunk::filePath).toList();
    }

    @Override
    public List<Context> findContext(String projectId, String prompt, int contextEntries) {
        Project project = findProjectById(projectId).orElseThrow(() ->
                new RuntimeException("Project not found with ID: " + projectId));

        return findContexts(project.getId(), prompt, contextEntries);
    }

    private List<Context> findContexts(String projectId, String query, int limit) {
        Set<FileSection> fileSections = fetchFileSections(projectId);
        if (fileSections.isEmpty()) {
            throw new RuntimeException("No such project in database");
        }

        List<Double> queryEmbedding = embeddingService.getEmbeddingResponse(Collections.singletonList(query))
                .getResult().getOutput();

        List<Context> contexts = fileSections.stream()
                .map(fileSection -> new Context(
                        1.0 - MathUtils.cosineSimilarity(queryEmbedding, fileSection.getEmbeddings()) - fileSection.getWeight(),
                        fileSection.getContent(), fileSection.getId(), fileSection.getWeight()))
                .sorted(Comparator.comparing(Context::getDistance))
                .collect(Collectors.toList());

        return contexts.subList(0, Math.min(contexts.size(), limit));
    }

    private Set<FileSection> fetchFileSections(String projectId) {
        // First, check the cache for existing file sections.
        Set<FileSection> cachedSections = projectFileSectionsCache.get(projectId);
        if (cachedSections != null && !cachedSections.isEmpty()) {
            return cachedSections;
        }

        // If not in cache, fetch from database.
        Set<FileSection> fileSections = new HashSet<>();
        int pageNumber = 0;
        final int pageSize = 150;

        Page<FileSection> page;
        do {
            PageRequest pageRequest = PageRequest.of(pageNumber++, pageSize);
            page = fileSectionRepository.findAllByProjectId(projectId, pageRequest);
            fileSections.addAll(page.getContent());
        } while (page.hasNext());

        // Update the cache with the newly fetched file sections.
        projectFileSectionsCache.put(projectId, fileSections);

        return fileSections;
    }

    private void associateFileChunkWithSections(FileChunk fileChunk, Set<FileSection> sections) {
        if (fileChunk == null || sections == null) {
            throw new IllegalArgumentException("FileChunk and sections cannot be null.");
        }

        Project project = projectRepository.findById(fileChunk.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + fileChunk.projectId()));

        fileRepository.findByProjectIdAndChecksum(fileChunk.projectId(), fileChunk.checksum())
                .orElseGet(() -> {
                    clearRelatedFileSections(fileChunk);
                    File file = new File(project, fileChunk.checksum(), fileChunk.filePath());
                    sections.forEach(section -> section.setFile(file));
                    file.setSections(sections);
                    fileRepository.save(file);
                    return file;
                });
    }

    private void clearRelatedFileSections(FileChunk fileChunk) {
        Optional.ofNullable(projectFileSectionsCache.get(fileChunk.projectId()))
                .ifPresent(Set::clear);

        fileRepository.findByProjectIdAndPath(fileChunk.projectId(), fileChunk.filePath())
                .ifPresent(file -> fileRepository.deleteById(file.getId()));
    }
}
