package com.qassistant.context.services.context.systemMessage;

import com.qassistant.context.configs.ContextConfig;
import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.Context;
import com.qassistant.context.entities.SystemMessageContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean({DbService.class})
public class SystemContextFormatterService implements ContextFormater{
    private final DbService dbService;
    private final ContextConfig contextConfig;

    public SystemContextFormatterService(DbService dbService, ContextConfig contextConfig) {
        this.dbService = dbService;
        this.contextConfig = contextConfig;
    }

    @Override
    public SystemMessageContext formatContextToSystemMessage(String project, String formatString, String prompt) {
        List<Context> contexts = dbService.findContext(project, prompt, contextConfig.getContextEntries());
        if (contexts.isEmpty()) {
            throw new RuntimeException("Context is empty");
        } else {
            String formattedContent = contexts.stream()
                    .map(Context::getContent)
                    .collect(Collectors.joining("\n"));
            List<String> contextIds = contexts.stream()
                    .map(Context::getId)
                    .collect(Collectors.toList());
            return new SystemMessageContext(formatString.formatted(formattedContent), contextIds);
        }
    }
}
