package ai.service.memory;

import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Map;

public class AssistantMessageUtil extends AssistantMessage {
    @Getter @Setter
    private Map<String, Object> params;

    public AssistantMessageUtil(String content,
                              Map<String, Object> properties,
                              List<ToolCall> toolCalls, List<Media> media, Map<String, Object> params) {
        super(content, properties, toolCalls, media);
        this.params = params;
    }

}
