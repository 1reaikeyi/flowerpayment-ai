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

@Service
@Slf4j
public class ToolFunction implements NodeAction {

    @Autowired
    private ToolService toolService;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        Object visualValue = state.value("visualResult").orElse(null);
        String input = visualValue == null ? "图片出错" : visualValue.toString();

        // question 同样从解包后的值取，缺失时给默认提示
        String question = visualValue == null ? "查询信息" : visualValue.toString();
        PromptTemplate promptTemplate = new PromptTemplate(
                "你是一个花店销售。根据用户描述{input}作为查询条件，并解决用户的问题 {question}。");
        String prompt = promptTemplate.render(
                Map.of("input", input, "question", question)
        );

        String result =  toolService.chat(prompt)
                .collectList()
                .timeout(Duration.ofSeconds(80))
                .blockOptional(Duration.ofSeconds(120))
                .toString();
        return Map.of("toolResult", result);
    }

}
