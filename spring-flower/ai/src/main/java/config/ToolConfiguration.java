package config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.tool.PlanTool;


@Configuration
public class ToolConfiguration {
    @Bean
    public ChatClient toolClient(OpenAiChatModel model,
                                 @Qualifier("loggerAdvisor") Advisor loggerAdvisor,
                                 @Qualifier("memoryAdvisor") Advisor messageMemoryAdvisor,
                                 PlanTool planTool) {  // 日志记录器)
        return ChatClient.builder(model)
                .defaultAdvisors(loggerAdvisor, messageMemoryAdvisor)
                .defaultTools(planTool)
                .build();
    }

}
