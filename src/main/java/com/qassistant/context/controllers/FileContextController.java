package com.qassistant.context.controllers;

import com.qassistant.context.services.context.FileContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.qassistant.context.db.service.DbService;

@RestController
@Tag(name = "Context")
@RequestMapping(value = "/fileContext")
@ConditionalOnProperty(prefix = "application.gpt", name = {"embeddingsModel"})
@ConditionalOnBean(DbService.class)
public class FileContextController {
    private final FileContextService fileContextService;

    public FileContextController(FileContextService fileContextService) {
        this.fileContextService = fileContextService;
    }

    /**
     * Creates a file context for the specified project and index path.
     * @param projectId The project identifier.
     * @param indexPath The path used for indexing files.
     * @return ResponseEntity containing the result and the HTTP status.
     */
    @Operation(description = "Creates a file context for the given project.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully created file context")})
    @RequestMapping(method = RequestMethod.POST, path = "/createFileContext")
    public ResponseEntity<Object> createFileContext(
            @Parameter(description = "Project identifier", required = true) @RequestParam("project") String projectId,
            @Parameter(description = "Index path", required = true) @RequestParam("indexPath") String indexPath) {

        Object fileContextResult = fileContextService.findAndIndexFiles(projectId, indexPath);
        return ResponseEntity.ok(fileContextResult);
    }
}
