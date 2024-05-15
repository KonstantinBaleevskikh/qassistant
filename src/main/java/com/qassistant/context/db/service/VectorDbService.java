package com.qassistant.context.db.service;

import com.qassistant.context.db.dbEntity.File;
import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.entities.Context;
import com.qassistant.context.entities.FileChunk;
import com.qassistant.context.utils.TextUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.driver.summary.ResultSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile(value={"prod"})
@ConditionalOnProperty(prefix="spring.neo4j", name={"uri"})
@ConditionalOnBean(Driver.class)
public class VectorDbService implements DbService {
    private static final Logger logger = LoggerFactory.getLogger(VectorDbService.class);
    private final VectorStore vectorStore;
    private final Driver neo4jDriver;

    public VectorDbService(VectorStore vectorStore, Driver neo4jDriver) {
        this.vectorStore = vectorStore;
        this.neo4jDriver = neo4jDriver;
    }


    @Override
    public Project saveProject(Project project) {
        try (Session session = neo4jDriver.session()) {
            // Check if a project with the same name already exists
            if (findProjectByName(project.getName()).isPresent()) {
                throw new IllegalArgumentException("A project with the name '" + project.getName() + "' already exists.");
            }

            // Create a new project node in Neo4j
            String cypherQuery = "CREATE (p:Project {id: randomUUID(), name: $name}) RETURN p.id AS generatedId";
            Record record = session.run(cypherQuery, Map.of("name", project.getName())).single();

            // Set the generated UUID on the project object
            project.setId(record.get("generatedId").asString());
            return project;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save project: " + project.getName(), e);
        }
    }

    @Override
    public void deleteProject(String projectId) {
        try (Session session = neo4jDriver.session()) {
            // Retrieve the project or throw if it doesn't exist
            Project project = findProjectById(projectId).orElseGet(() -> findProjectByName(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found: " + projectId)));

            // Delete the project and all related entities from Neo4j
            String cypherQuery = "MATCH (p:Project {id: $projectId}) " +
                    "OPTIONAL MATCH (p)-[r*0..]-(d) " +
                    "DETACH DELETE p, d";
            session.run(cypherQuery, Map.of("projectId", project.getId()));

            logger.info("Successfully deleted project with ID: {}", projectId);
        } catch (Exception e) {
            logger.error("Failed to delete project with ID: {}", projectId, e);
            throw new RuntimeException("Failed to delete project: " + projectId, e);
        }
    }

    @Override
    public Optional<Project> findProjectById(String id) {
        try (Session session = neo4jDriver.session()) {
            // Query to find a project by its unique identifier
            String cypherQuery = "MATCH (p:Project) WHERE p.id = $projectId RETURN p.id as id, p.name as name";
            Record record = session.run(cypherQuery, Map.of("projectId", id)).single();

            // Construct and return the Project object if found
            Project project = new Project(record.get("id").asString(), record.get("name").asString());
            return Optional.of(project);
        } catch (NoSuchRecordException e) {
            logger.warn("No project found with ID: {}", id);  // Log when no project is found
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find project by ID: {}", id, e);  // Log other exceptions
            throw new RuntimeException("Failed to retrieve project with ID: " + id, e);
        }
    }

    @Override
    public Optional<Project> findProjectByName(String name) {
        try (Session session = neo4jDriver.session()) {
            // Construct the Cypher query to find the project by name
            String cypherQuery = "MATCH (p:Project) WHERE p.name = $projectName RETURN p.id AS id, p.name AS name";
            Record record = session.run(cypherQuery, Map.of("projectName", name)).single();

            // Construct and return the project from the retrieved record
            Project project = new Project(record.get("id").asString(), record.get("name").asString());
            return Optional.of(project);
        } catch (NoSuchRecordException e) {
            // Log and return an empty Optional when no project is found
            logger.warn("No project found with name: {}", name);
            return Optional.empty();
        } catch (Exception e) {
            // Log any other exceptions that might occur during the query execution
            logger.error("Failed to find project by name: {}", name, e);
            throw new RuntimeException("Failed to retrieve project with name: " + name, e);
        }
    }

    @Override
    public Iterable<Project> findAllProjects() {
        try (Session session = neo4jDriver.session()) {
            // Execute a Cypher query to retrieve all projects
            List<Record> records = session.run("MATCH (p:Project) RETURN p.id AS id, p.name AS name").list();

            // Transform the results into a list of Project objects
            List<Project> projects = records.stream()
                    .map(record -> new Project(record.get("id").asString(), record.get("name").asString()))
                    .collect(Collectors.toList());

            return projects;
        } catch (Exception e) {
            logger.error("Failed to retrieve all projects", e);
            throw new RuntimeException("Error retrieving all projects", e);
        }
    }

    @Override
    public void deleteAllFilesByProject(String projectId) {
        try (Session session = neo4jDriver.session()) {
            Project project = findProjectById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

            String cypherQuery = "MATCH (f:File {projectId: $projectId}) " +
                    "OPTIONAL MATCH (f)-[r*0..]->(d) " +
                    "DETACH DELETE f, d";
            ResultSummary resultSummary = session.run(cypherQuery, Map.of("projectId", project.getId())).consume();

            if (resultSummary.counters().nodesDeleted() == 0) {
                logger.warn("No files were deleted for project ID: {}", projectId);
            } else {
                logger.info("All files deleted for project ID: {}", projectId);
            }
        } catch (Exception e) {
            logger.error("Failed to delete all files for project ID: {}", projectId, e);
            throw new RuntimeException("Error deleting all files for project ID: " + projectId, e);
        }
    }

    @Override
    public void deleteFileByProjectAndName(String projectId, String fileName) {
        try (Session session = neo4jDriver.session()) {
            Project project = findProjectById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

            String cypherQuery = "MATCH (f:File {projectId: $projectId, fileName: $fileName}) " +
                    "OPTIONAL MATCH (f)-[r*0..]->(d) " +
                    "DETACH DELETE f, d";
            ResultSummary resultSummary = session.run(cypherQuery, Map.of("projectId", project.getId(), "fileName", fileName)).consume();

            if (resultSummary.counters().nodesDeleted() == 0) {
                logger.warn("No such file found with name: {} for project ID: {}", fileName, projectId);
                throw new RuntimeException("File not found or no files deleted with name: " + fileName + " for project ID: " + projectId);
            } else {
                logger.info("File named '{}' deleted for project ID: {}", fileName, projectId);
            }
        } catch (Exception e) {
            logger.error("Failed to delete file '{}' for project ID: {}", fileName, projectId, e);
            throw new RuntimeException("Error deleting file with name: " + fileName + " for project ID: " + projectId, e);
        }
    }

    @Override
    public int countFilesByProject(String projectId) {
        try (Session session = neo4jDriver.session()) {
            // Find the project by ID, and if not found, try by name or throw an exception if neither found.
            Project project = findProjectById(projectId).orElseGet(() ->
                    findProjectByName(projectId).orElseThrow(() ->
                            new RuntimeException("No project found with ID or name: " + projectId)));

            // Query to count the files associated with the project ID.
            String cypherQuery = "MATCH (f:File) WHERE f.projectId = $projectId RETURN COUNT(f) AS fileCount";
            Record record = session.run(cypherQuery, Map.of("projectId", project.getId())).single();

            // Return the count of files found.
            return record.get("fileCount").asInt();
        } catch (Exception e) {
            logger.error("Failed to count files for project ID: {}", projectId, e);
            throw new RuntimeException("Error counting files for project ID: " + projectId, e);
        }
    }

    @Override
    public double setWeightForFile(String projectId, String fileName, double weight) {
        try (Session session = neo4jDriver.session()) {
            // Ensure the project exists, or throw an informative error if not found
            Project project = findProjectById(projectId).orElseGet(() ->
                    findProjectByName(projectId).orElseThrow(() ->
                            new RuntimeException("Project not found with ID: " + projectId)));

            // Update the weight metadata for the specified file
            String cypherQuery = "MATCH (f:File {projectId: $projectId, fileName: $fileName}) " +
                    "OPTIONAL MATCH (f)-[r*1..]->(d) " +
                    "WHERE d IS NOT NULL " +
                    "SET d.`metadata.weight` = $weightValue " +
                    "RETURN d.metadata.weight AS newWeight";
            Record record = session.run(cypherQuery, Map.of(
                    "projectId", project.getId(),
                    "fileName", fileName,
                    "weightValue", weight
            )).single();

            // Return the updated weight to confirm the operation was successful
            return record.get("newWeight").asDouble();
        } catch (Exception e) {
            logger.error("Failed to set weight for file '{}' in project '{}': {}", fileName, projectId, e);
            throw new RuntimeException("Error setting weight for file '" + fileName + "' in project ID: " + projectId, e);
        }
    }

    @Override
    public double setWeightForSections(String projectId, List<String> ids, double weight) {
        try (Session session = neo4jDriver.session()) {
            // Ensure the project exists to associate the document weights correctly
            Project project = findProjectById(projectId).orElseGet(() ->
                    findProjectByName(projectId).orElseThrow(() ->
                            new RuntimeException("Project not found with ID: " + projectId)));

            // Construct and execute a Cypher query to update the weight metadata for documents
            String cypherQuery = "MATCH (d:Document) WHERE d.id IN $ids AND d.projectId = $projectId " +
                    "SET d.metadata.weight = $weightValue RETURN d.metadata.weight AS newWeight";
            List<Record> records = session.run(cypherQuery, Map.of(
                    "projectId", project.getId(),
                    "ids", ids,
                    "weightValue", weight
            )).list();

            // Log the update for monitoring
            if (records.isEmpty()) {
                logger.warn("No documents found or updated for project ID: {} with provided IDs.", projectId);
                return 0;
            } else {
                logger.info("Weight set to {} for documents in project ID: {}", weight, projectId);
                return weight; // Return the new weight to confirm the update
            }
        } catch (Exception e) {
            logger.error("Failed to set weight for documents in project ID: {}", projectId, e);
            throw new RuntimeException("Error setting weight for documents in project ID: " + projectId, e);
        }
    }

    @Override
    public Map<String, String> indexClassification(String projectId, Map<String, String> promptsAndAnswers, String filePath) {
        Project project = findProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        Map<File, List<Document>> fileDocumentMap = indexPromptBasedDocuments(project, promptsAndAnswers, filePath);
        fileDocumentMap = filterAndUpdateDocumentMap(project.getId(), fileDocumentMap);

        if (fileDocumentMap.isEmpty()) {
            logger.info("No documents were indexed for project ID: {}", projectId);
            return Collections.emptyMap();
        }

        indexAndLinkDocuments(projectId, fileDocumentMap);
        logger.info("Classification indexed for project ID: {}", projectId);
        return promptsAndAnswers;
    }

    @Override
    public List<String> indexChunkResult(ChunkResult chunkResult) {
        if (chunkResult.fileChunks().isEmpty()) {
            throw new IllegalArgumentException("fileChunks size should be more than 0");
        }

        String projectId = chunkResult.fileChunks().get(0).projectId();
        Map<File, List<Document>> fileDocumentMap = indexDocumentsFromChunks(projectId, chunkResult.fileChunks());
        fileDocumentMap = filterAndUpdateDocumentMap(projectId, fileDocumentMap);

        if (fileDocumentMap.isEmpty()) {
            logger.warn("No documents were indexed from chunks for project ID: {}", projectId);
            return Collections.emptyList();
        }

        indexAndLinkDocuments(projectId, fileDocumentMap);
        logger.info("Chunk results indexed for project ID: {}", projectId);
        return chunkResult.fileChunks().stream()
                .map(FileChunk::filePath)
                .collect(Collectors.toList());
    }

    @Override
    public List<Context> findContext(String projectId, String prompt, int contextEntries) {
        Project project2 = this.findProjectById(projectId).orElseGet(() -> this.findProjectByName(projectId).orElseThrow(() -> new RuntimeException("There is no such project")));
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        List<Document> list = vectorStore.similaritySearch(SearchRequest.defaults().withQuery(prompt).withTopK(contextEntries).withFilterExpression(filterExpressionBuilder.or(filterExpressionBuilder.eq("projectName", (Object)project2.getName()), filterExpressionBuilder.eq("projectId", (Object)project2.getId())).build()));
        List<Context> contexts;  // Collecting the results into a list
        contexts = list.stream()
                .map(document -> {
                    String answer = Optional.ofNullable(document.getMetadata().get("answer"))
                            .map(Object::toString)
                            .orElseGet(document::getContent);
                    double distance = Optional.ofNullable(document.getMetadata().get("distance"))
                            .map(Object::toString)
                            .map(Double::parseDouble)
                            .orElse(0.0);
                    double weight = Optional.ofNullable(document.getMetadata().get("weight"))
                            .map(Object::toString)
                            .map(Double::parseDouble)
                            .orElse(0.0);

                    // Calculate the adjusted distance by subtracting the weight
                    double adjustedDistance = distance - weight;

                    return new Context(adjustedDistance, answer, document.getId(), weight);
                })
                .sorted(Comparator.comparing(Context::getDistance))  // Sorting by the distance field
                .collect(Collectors.toList());

        return contexts;
    }


    private Map<File, List<Document>> indexDocumentsFromChunks(String projectId, List<FileChunk> fileChunks) {
        Project project = findProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with ID '" + projectId + "' does not exist"));

        Map<File, List<Document>> fileDocumentsMap = new HashMap<>();
        for (FileChunk chunk : fileChunks) {
            File file = new File(project, chunk.checksum(), chunk.filePath());
            List<Document> documents = chunk.sections().stream()
                    .map(section -> new Document(section, Map.of(
                            "projectId", project.getId(),
                            "filePath", chunk.filePath(),
                            "weight", 0,
                            "projectName", project.getName())))
                    .collect(Collectors.toList());

            fileDocumentsMap.put(file, documents);
        }
        return fileDocumentsMap;
    }


    private Map<File, List<Document>> indexPromptBasedDocuments(Project project, Map<String, String> prompts, String filePath) {
        List<Document> documents = new ArrayList<>();
        String combinedKeys = String.join(", ", prompts.keySet());
        String checksum = TextUtils.generateSha256(combinedKeys);

        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            Document document = new Document(
                    entry.getKey(),
                    Map.of(
                            "projectId", project.getId(),
                            "filePath", filePath,
                            "weight", 0,
                            "answer", entry.getValue(),
                            "projectName", project.getName()
                    )
            );
            documents.add(document);
        }

        File file = new File(project, checksum, filePath);
        return Map.of(file, documents);
    }


    private void indexAndLinkDocuments(String projectId, Map<File, List<Document>> fileDocumentsMap) {
        for (Map.Entry<File, List<Document>> entry : fileDocumentsMap.entrySet()) {
            File file = createFileNode(entry.getKey());
            List<Document> documents = entry.getValue();

            // Calculate the number of documents to handle per thread based on available processors
            int batchSize = Math.max(1, documents.size() / Runtime.getRuntime().availableProcessors());

            // Handle documents in parallel batches
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < documents.size(); i += batchSize) {
                int end = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, end);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> vectorStore.add(batch));
                futures.add(future);
            }

            // Wait for all futures to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                allFutures.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted during document addition", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Error adding documents to vector store", e.getCause());
            }

            // Link documents to the file node in Neo4j
            linkDocumentsToFile(projectId, file, documents);
        }
    }

    private void linkDocumentsToFile(String projectId, File file, List<Document> documents) {
        List<String> documentIds = documents.stream().map(Document::getId).toList();
        if (!documentIds.isEmpty()) {
            try (Session session = neo4jDriver.session()) {
                String cypher = "MATCH (p:Project {id: $projectId}), (f:File {id: $fileId}) " +
                        "FOREACH (docId IN $docIds | " +
                        "   MERGE (d:Document {id: docId}) " +
                        "   MERGE (f)-[:CONTAINS]->(d))";
                session.run(cypher, Map.of("projectId", projectId, "fileId", file.getId(), "docIds", documentIds));
            } catch (Exception e) {
                throw new RuntimeException("Failed to link documents to file in Neo4j", e);
            }
        }
    }

    private Map<File, List<Document>> filterAndUpdateDocumentMap(String projectId, Map<File, List<Document>> fileDocumentMap) {
        fileDocumentMap.entrySet().removeIf(entry -> {
            File file = entry.getKey();
            Optional<File> existingFile = findFile(projectId, file.getPath());
            if (existingFile.isPresent()) {
                if (!file.getChecksum().equals(existingFile.get().getChecksum())) {
                    deleteFileByChecksum(existingFile.get().getChecksum());
                    return false;
                }
                return true;
            }
            return false;
        });
        return new HashMap<>(fileDocumentMap);
    }

    private Optional<File> findFile(String projectId, String filePath) {
        // Ensure the project exists before proceeding
        Project project = findProjectById(projectId).orElseGet(() ->
                findProjectByName(projectId).orElseThrow(() ->
                        new RuntimeException("Project not found with ID: " + projectId)));

        try (Session session = neo4jDriver.session()) {
            // Query to find the file with the specified projectId and filePath
            String query = "MATCH (f:File) WHERE f.projectId = $projectId AND f.path = $filePath RETURN f.path AS path, f.checksum AS checksum";
            Record record = session.run(query, Map.of("projectId", projectId, "filePath", filePath)).single();

            // Construct and return the File object from the results
            return Optional.of(new File(project, record.get("path").asString(), record.get("checksum").asString()));
        } catch (NoSuchRecordException e) {
            // Return empty if no such file is found
            return Optional.empty();
        } catch (Exception e) {
            // Log error and rethrow if a different exception occurs
            throw new RuntimeException("Failed to retrieve file from database for project ID: " + projectId + " and file path: " + filePath, e);
        }
    }

    private File createFileNode(File file) {
        try (Session session = neo4jDriver.session()) {
            String cypherQuery = "CREATE (f:File {id: randomUUID(), projectId: $projectId, fileName: $fileName, checksum: $checksum, path: $path}) RETURN f.id AS generatedId";
            Record record = session.run(cypherQuery, Map.of(
                    "projectId", file.getProject().getId(),
                    "fileName", file.getName(),
                    "checksum", file.getChecksum(),
                    "path", file.getPath()
            )).single();
            file.setId(record.get("generatedId").asString());
            return file;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create file node in Neo4j", e);
        }
    }

    private int deleteFileByChecksum(String checksum) {
        try (Session session = neo4jDriver.session()) {
            // Run the delete query
            String query = "MATCH (f:File {checksum: $checksum}) " +
                    "OPTIONAL MATCH (f)-[r*0..]->(d) " +
                    "DETACH DELETE f, d RETURN count(f) AS filesDeleted, count(d) AS relatedDeleted";
            Record result = session.run(query, Map.of("checksum", checksum)).single();

            // Calculate the total number of entities deleted
            int filesDeleted = result.get("filesDeleted").asInt();
            int relatedDeleted = result.get("relatedDeleted").asInt();
            return filesDeleted + relatedDeleted;
        } catch (NoSuchRecordException e) {
            // No files or related entities found to delete
            return 0;
        } catch (Exception e) {
            // Log and rethrow the error with a more informative message
            throw new RuntimeException("Failed to delete file with checksum: " + checksum, e);
        }
    }
}
