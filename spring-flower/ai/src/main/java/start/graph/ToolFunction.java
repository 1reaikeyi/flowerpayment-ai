package start.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ToolFunction implements NodeAction {

    @Resource(name = "toolClient")
    private ChatClient chatClient;
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = Optional.ofNullable(state.value("visual"))
                .map(Object::toString)
                .orElse("视觉识别出错");
        PromptTemplate promptTemplate = new PromptTemplate("你负责查询"+
                "根据信息{input}查询");
        promptTemplate.add("input", input);

        Flux<String> result = chat(input);
        return Map.of("toolResult", result);
    }

    private Flux<String> chat(String qusetion){
        return chatClient.prompt()
                .user(qusetion)
                .stream()
                .content();
    }
}
