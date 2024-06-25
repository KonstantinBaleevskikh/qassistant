package com.qassistant.context.services.slack;


import com.qassistant.context.configs.SlackConfig;
import com.qassistant.context.services.slack.events.MessageToBotHandler;
import com.qassistant.context.services.slack.events.RegenerateHandler;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.event.MessageChangedEvent;
import com.slack.api.model.event.MessageDeletedEvent;
import com.slack.api.model.event.MessageEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.logging.Logger;

@Component
@ConditionalOnExpression("'${application.bot}'.contains('slack')")
public class SlackBot {
    private final SlackConfig slackConfig;
    private final Counter successCounter;
    private final Counter failureCounter;
    Logger log = Logger.getLogger(SlackBot.class.getName());

    public SlackBot(SlackConfig slackConfig, Environment environment) {
        this.slackConfig = slackConfig;
        String activeProfile = Arrays.stream(environment.getActiveProfiles()).findFirst().orElse("default");
        this.successCounter = Metrics.counter(
                "success_counter",
                "environment", activeProfile,
                "description", "Count of success answers"
        );
        this.failureCounter = Metrics.counter(
                "failure_counter",
                "environment", activeProfile,
                "description", "Count of failure answers"
        );
    }

    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(slackConfig.getBotToken())
                .signingSecret(slackConfig.getSigningSecret())
                .build();
    }

    @Bean
    public SocketModeApp initSlackApp(
            AppConfig config,
            MessageToBotHandler messageToBotHandler,
            RegenerateHandler regenerateHandler
    ) throws Exception {
        App app = new App(config);
        if (config.getClientId() != null) app.asOAuthApp(true);
        //EVENTS
        app.event(MessageChangedEvent.class, (payload, ctx) -> ctx.ack());
        app.event(MessageDeletedEvent.class, (payload, ctx) -> ctx.ack());
        app.event(MessageEvent.class, messageToBotHandler);
        //ACTIONS
        app.blockAction(SlackCommand.regenerate.name(), regenerateHandler);
        app.blockAction(SlackCommand.thumbs_down.name(), (payload, ctx) -> {
            failureCounter.increment();
            return ctx.ack();
        });
        app.blockAction(SlackCommand.thumbs_up.name(), (payload, ctx) -> {
            successCounter.increment();
            return ctx.ack();
        });
        SocketModeApp socketModeApp = new SocketModeApp(slackConfig.getAppToken(), app);
        socketModeApp.startAsync();
        return socketModeApp;
    }
}
