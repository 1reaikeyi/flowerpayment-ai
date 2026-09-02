package ai.service.chat;

import ai.model.enums.ChatEventTypeEnum;
import ai.model.vo.ChatEventVO;
import ai.service.session.SessionService;
import ai.start.config.PromptConfig;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Service
public class ChatImpl implements Chat {
    //src/main/java/start/config/ChatConfiguratioon.java::chatClient
    @Resource(name = "chatClient")
    private ChatClient chatClient;
    @Autowired
    private PromptConfig systemPromptConfig;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private SessionService sessionService;

    private final static String OUTPUT_STATUS = "OUTPUT_STATUS";

    /**
     * chat
     *
     * @param question  问题
     * @param sessionId 会话id
     * @return 回答内容
     */
    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {

        sessionService.updateSessionTitle(sessionId, question);
        /**
         * 实现类 memory.MysqlChatMemoryRepository.java
         * public List<Message> findByConversationId(String conversationId) {}自动查询加入session
         */
        var conversationId = Chat.getConversationId(sessionId);

        //控制是否stop+追加保存
        var outputHash = stringRedisTemplate.boundHashOps(OUTPUT_STATUS);
        var outputBuilder = new StringBuilder();

        return chatClient.prompt()
                .user(question)
                .advisors(advisorSpec -> advisorSpec
                        //会话记忆
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .system(promptSystemSpec -> promptSystemSpec
                        //系统role
                        .text(systemPromptConfig.getChatSystemMessage().get())
                        .param("now", LocalDateTime.now())
                )
                .stream()
                .chatResponse()
                // 第一次输出内容时执行
                // 出现异常时，删除标识
                // 完成时执行，删除标识
                .doFirst(() -> outputHash.put(sessionId, "true"))  // 将布尔值转换为字符串存入 Redis
                .doOnError(throwable -> outputHash.delete(sessionId))
                .doOnComplete(() -> outputHash.delete(sessionId))
                .doOnCancel(() -> {
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                //控制是否继续
                .takeWhile(chatResponse -> outputHash.get(sessionId) != null)

                .map(chatResponse -> {
                    String response = chatResponse.getResult().getOutput().getText();
                    // 追加到输出内容中
                    outputBuilder.append(response);
                    ChatEventVO chatEventVO = ChatEventVO.builder()
                            .eventData(response)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                    return chatEventVO;
                })

                .concatWith(Flux.just(ChatEventVO.builder()
                                .eventType(ChatEventTypeEnum.STOP.getValue())
                                .build()));
    }

    /**
     * 保存停止输出的记录
     *
     * @param conversationId 会话id
     * @param content        大模型输出的内容
     */
    private void saveStopHistoryRecord(String conversationId, String content) {
        chatMemory.add(conversationId, new AssistantMessage(content));
    }

    /**
     * 停止生成
     *
     * @param sessionId 会话id
     */
    @Override
    public void stop(String sessionId) {
        var outputHash = stringRedisTemplate.boundHashOps(OUTPUT_STATUS);
        // 移除标记
        outputHash.delete(sessionId);
    }
}
