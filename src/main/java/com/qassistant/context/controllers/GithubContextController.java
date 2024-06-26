package com.qassistant.context.controllers;

import com.qassistant.context.entities.ChunkResult;
import com.qassistant.context.services.context.ContextService;
import com.qassistant.context.services.context.GithubContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "GithubContext")
@RequestMapping(value = "/githubContext")
@ConditionalOnProperty(prefix = "application.github", name = {"token", "userName"})
@ConditionalOnBean(GithubContextService.class)
public class GithubContextController {
    private final GithubContextService githubContextService;
    private final ContextService contextService;

    public GithubContextController(GithubContextService githubContextService, ContextService contextService) {
        this.githubContextService = githubContextService;
        this.contextService = contextService;
    }

    @Operation(summary = "Create GitHub Context")
    @ApiResponse(responseCode = "200", description = "OK")
    @PostMapping("/createGithubContext")
    public ResponseEntity<Object> createGithubContext(
            @Parameter(description = "Project identifier", required = true)
            @RequestParam String projectId,

            @Parameter(description = "GitHub repository name", required = true)
            @RequestParam String repositoryName,

            @Parameter(description = "Index path in the repository", required = false)
            @RequestParam(defaultValue = "") String indexPath) {

        ChunkResult chunkResult = githubContextService.fetchAndIndexFiles(projectId, repositoryName, indexPath);
        return new ResponseEntity(this.contextService.indexChunkResult(chunkResult), HttpStatus.OK);
    }
}
