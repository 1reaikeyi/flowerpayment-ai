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
import service.ToolService;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ToolFunction implements NodeAction {

    @Autowired
    private ToolService toolService;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = Optional.ofNullable(state.value("visual"))
                .map(Object::toString)
                .orElse("视觉识别出错");
        Object question = state.value("visual");

        if (question == null) {
            question = "查询信息";
        }
        question = "解答" + question;

        PromptTemplate promptTemplate = new PromptTemplate("你需要{question}"+
                "根据信息{input}查询");
        String prompt = promptTemplate.render(
                Map.of("question", "回答用户问题", "input", "今天天气晴朗")
        );

        String result =  toolService.chat(prompt)
                .collectList()
                .timeout(Duration.ofSeconds(30))
                .blockOptional(Duration.ofSeconds(60))
                .toString();
        return Map.of("toolResult", result);
    }

}
