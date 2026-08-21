package config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VisualConfiguration {
    @Bean
    public ChatClient visualChatClient(OpenAiChatModel model,
                                       @Qualifier("loggerAdvisor") Advisor loggerAdvisor,
                                       @Qualifier("memoryAdvisor") Advisor messageMemoryAdvisor) {  // 日志记录器
        return ChatClient.builder(model)
                .defaultAdvisors(loggerAdvisor, messageMemoryAdvisor)
                .build();
    }
}
