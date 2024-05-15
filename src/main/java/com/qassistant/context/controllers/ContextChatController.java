package com.qassistant.context.controllers;

import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.StyleWithContext;
import com.qassistant.context.entities.SystemMessageContext;
import com.qassistant.context.services.context.chat.ChatContextService;
import com.qassistant.context.utils.TextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Chat")
@RequestMapping("/chat")
@ConditionalOnProperty(prefix = "application.gpt", name = "embeddingsModel")
@ConditionalOnBean(DbService.class)
public class ContextChatController {
    private final ChatContextService<Message> chatContextService;

    public ContextChatController(ChatContextService<Message> chatContextService) {
        this.chatContextService = chatContextService;
    }

    @Operation(summary = "Handle Chat Context", description = "Processes a chat context completion request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping(path = "/contextChat", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<Object> handleChatContext(
            @Parameter(description = "The chat prompt") @RequestParam("prompt") String prompt,
            @Parameter(description = "Project identifier") @RequestParam("project") String projectId,
            HttpServletRequest request) {

        SystemMessageContext systemMessageContext = chatContextService.formatSystemMessageWithContext(
                projectId,
                StyleWithContext.CODE_CONTEXT.getSystemMessage().message(TextUtils.containsCyrillic(prompt)),
                prompt
        );
        chatContextService.setSystemMessage(systemMessageContext.getSystemMessage(), request.getSession().getId());

        return ResponseEntity.ok(chatContextService.completionChat(prompt, request.getSession().getId()));
    }
}
