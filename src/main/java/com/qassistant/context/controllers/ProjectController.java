package com.qassistant.context.controllers;

import com.qassistant.context.db.dbEntity.Project;
import com.qassistant.context.db.service.DbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Projects in db")
@RequestMapping("/projects")
@ConditionalOnBean(DbService.class)
public class ProjectController {
    private final DbService dbService;

    public ProjectController(DbService dbService) {
        this.dbService = dbService;
    }

    @Operation(description = "Create project")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @PostMapping(path = "/createProject", produces = "application/json")
    public ResponseEntity<Object> createProject(
            @Parameter(description = "name", required = true) @RequestParam(name = "name") String name) {
        Project newProject = new Project(name);
        return new ResponseEntity<>(dbService.saveProject(newProject), HttpStatus.OK);
    }

    @Operation(description = "List of projects")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @GetMapping(path = "/listOfProjects", produces = "application/json")
    public ResponseEntity<Object> listProjects() {
        return new ResponseEntity<>(dbService.findAllProjects(), HttpStatus.OK);
    }

    @Operation(description = "Delete project")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @DeleteMapping(path = "/deleteProject")
    public ResponseEntity<Object> deleteProject(
            @Parameter(description = "project", required = true) @RequestParam(name = "project") String projectId) {
        dbService.deleteProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(description = "Count files of project")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @GetMapping(path = "/countFilesOfProject")
    public ResponseEntity<Object> countFilesOfProject(
            @Parameter(description = "project", required = true) @RequestParam(name = "project") String projectId) {
        return new ResponseEntity<>(dbService.countFilesByProject(projectId), HttpStatus.OK);
    }

    @Operation(description = "Set weight for file")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @PostMapping(path = "/setWeightForFile")
    public ResponseEntity<Object> setWeightForFile(
            @Parameter(description = "project", required = true) @RequestParam(name = "project") String projectId,
            @Parameter(description = "fileName", required = true) @RequestParam(name = "fileName") String fileName,
            @Parameter(description = "weight", required = true) @RequestParam(name = "weight") double weight) {
        double updatedWeight = dbService.setWeightForFile(projectId, fileName, weight);
        return new ResponseEntity<>(updatedWeight, HttpStatus.OK);
    }

    @Operation(description = "Set weight for sections")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @PostMapping(path = "/setWeightForSections")
    public ResponseEntity<Object> setWeightForSections(
            @Parameter(description = "project", required = true) @RequestParam(name = "project") String projectId,
            @Parameter(description = "ids", required = true) @RequestParam(name = "ids") List<String> sectionIds,
            @Parameter(description = "weight", required = true) @RequestParam(name = "weight") double weight) {
        double updatedWeight = dbService.setWeightForSections(projectId, sectionIds, weight);
        return new ResponseEntity<>(updatedWeight, HttpStatus.OK);
    }

    @Operation(description = "Delete all files from the project")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @DeleteMapping(path = "/deleteFilesFromTheProject")
    public ResponseEntity<Object> deleteAllFilesFromProject(
            @Parameter(description = "project", required = true) @RequestParam(name = "project") String projectId) {
        dbService.deleteAllFilesByProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(description = "Delete file from the project by name")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @DeleteMapping(path = "/deleteFileFromTheProjectByName")
    public ResponseEntity<Object> deleteFileFromProjectByName(
            @Parameter(description = "project", required = true) @RequestParam(name = "project") String projectId,
            @Parameter(description = "fileName", required = true) @RequestParam(name = "fileName") String fileName) {
        dbService.deleteFileByProjectAndName(projectId, fileName);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}