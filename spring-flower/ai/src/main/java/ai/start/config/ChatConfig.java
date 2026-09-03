package ai.start.config;

import ai.service.graph.tool.FlowerTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean
    public ChatClient chatClient(OpenAiChatModel model,
                                 @Qualifier("loggerAdvisor") Advisor loggerAdvisor,
                                 @Qualifier("memoryAdvisor") Advisor messageMemoryAdvisor,
                                 FlowerTool flowerTool) {  // 日志记录器)
        return ChatClient.builder(model)
                .defaultAdvisors(loggerAdvisor, messageMemoryAdvisor)
                .defaultTools(flowerTool)
                .build();
    }
    @Bean
    public ChatClient toolClient(OpenAiChatModel model,
                                 @Qualifier("loggerAdvisor") Advisor loggerAdvisor,
                                 @Qualifier("memoryAdvisor") Advisor messageMemoryAdvisor,
                                 FlowerTool flowerTool) {  // 日志记录器)
        return ChatClient.builder(model)
                .defaultAdvisors(loggerAdvisor, messageMemoryAdvisor)
                .defaultTools(flowerTool)
                .build();
    }
    @Bean
    public ChatClient visualChatClient(OpenAiChatModel model,
                                       @Qualifier("loggerAdvisor") Advisor loggerAdvisor) {  // 日志记录器
        return ChatClient.builder(model)
                .defaultAdvisors(loggerAdvisor)
                .build();
    }
}
