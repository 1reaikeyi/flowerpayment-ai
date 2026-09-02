package ai.start.config;


import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class PromptConfig {

    @Value("${system.chat.file:classpath:system-message.txt}")
    private String systemChatFilePath;

    private final AtomicReference<String> chatSystemMessage = new AtomicReference<>();

    @PostConstruct
    public void init() {
        loadSystemPrompt();
    }

    private void loadSystemPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(systemChatFilePath.replace("classpath:", ""));
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            chatSystemMessage.set(content);
            log.info("成功加载系统提示词文件: {}, 内容长度: {} 字符", systemChatFilePath, content.length());
        } catch (IOException e) {
            log.error("加载系统提示词文件失败: {}", systemChatFilePath, e);
            chatSystemMessage.set("");
        }
    }
}
