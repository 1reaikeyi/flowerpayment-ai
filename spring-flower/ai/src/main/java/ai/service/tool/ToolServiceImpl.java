package ai.service.tool;

import ai.model.enums.ChatEventTypeEnum;
import ai.model.vo.ChatEventVO;
import ai.service.ChatRecordService;
import ai.service.session.SessionService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ToolServiceImpl implements ToolService {
    @Resource(name = "toolClient")
    private ChatClient chatClient;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ChatRecordService chatRecordService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final static String  OUTPUT_STATUS = "OUTPUT_STATUS";
    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder()
            .eventType(ChatEventTypeEnum.STOP.getValue())
            .build();

    @Override
    public Flux<String> chat(String visualValue, String question) {
        PromptTemplate promptTemplate = new PromptTemplate(
                "你是一个花店查询家。根据用户描述{input}作为查询条件，并解决用户的问题 {question}。");
        String prompt = promptTemplate.render(
                Map.of("input", visualValue, "question", question)
        );
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

}