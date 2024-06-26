package com.qassistant.context.bots.slack.blocks;

import com.qassistant.context.bots.slack.SlackCommand;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AnswerBlock {

    public List<LayoutBlock> createAiMessage(String text) {
        BlockElement regenerateButton = ButtonElement.builder()
                .text(PlainTextObject.builder().text("Regenerate").build())
                .actionId(SlackCommand.regenerate.name())
                .build();
        BlockElement thumbsUpButton = ButtonElement.builder()
                .text(PlainTextObject.builder().text("👍").build())
                .actionId(SlackCommand.thumbs_up.name())
                .build();
        BlockElement thumbsDownButton = ButtonElement.builder()
                .text(PlainTextObject.builder().text("👎").build())
                .actionId(SlackCommand.thumbs_down.name())
                .build();
        List<BlockElement> blockElements = List.of(regenerateButton, thumbsUpButton, thumbsDownButton);
        return Blocks.asBlocks(
                Blocks.section(section -> section.text(BlockCompositions.markdownText(text.replaceAll("```\\S*\\s", "``` ")))),
                Blocks.actions(blockElements)
        );
    }
}
