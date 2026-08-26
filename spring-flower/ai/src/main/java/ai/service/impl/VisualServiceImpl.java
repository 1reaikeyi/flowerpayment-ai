package ai.service.impl;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ai.service.VisualService;

@Service
public class VisualServiceImpl implements VisualService {

    @Resource(name = "visualChatClient")
    private ChatClient visualChatClient;

    @Override
    public Flux<String> chat(Media media) {
        return visualChatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text("你是一个花艺信息提取助手。根据用户描述，从中提取以下三个字段：" +
                                "color（颜色，如：红色、粉色、白色。)"+
                                "description（花的特征)" +
                                "总结成数据格式 颜色=color,描述=description")
                        .media(media))
                .stream()
                .content();
    }
}
