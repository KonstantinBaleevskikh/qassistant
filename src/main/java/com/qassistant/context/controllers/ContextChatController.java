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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name="Chat")
@RequestMapping(value={"/chat/"})
@ConditionalOnProperty(prefix="application.gpt", name={"embeddingsModel"})
@ConditionalOnBean(value={DbService.class})
public class ContextChatController {
    private final ChatContextService<Message> chatContextService;

    public ContextChatController(ChatContextService<Message> chatContextService) {
        this.chatContextService = chatContextService;
    }

    @Operation(description="Completion simple chat")
    @ApiResponses(value={@ApiResponse(responseCode="200", description="OK")})
    @RequestMapping(method={RequestMethod.POST}, path={"/contextChat"}, produces={"text/plain;charset=UTF-8"})
    public ResponseEntity<Object> contextChat(
            @Parameter(description="prompt") @RequestParam(value="prompt") String prompt,
            @Parameter(description="project") @RequestParam(value="project") String project,
            HttpServletRequest httpServletRequest) {
        SystemMessageContext systemMessageContext = chatContextService.formatSystemMessageWithContext(project, StyleWithContext.CODE_CONTEXT.getSystemMessage().message(TextUtils.containsCyrillic(prompt)), prompt);
        chatContextService.setSystemMessage(systemMessageContext.getSystemMessage(), httpServletRequest.getSession().getId());
        return new ResponseEntity<>(chatContextService.completionChat(prompt, httpServletRequest.getSession().getId()), HttpStatus.OK);
    }
}