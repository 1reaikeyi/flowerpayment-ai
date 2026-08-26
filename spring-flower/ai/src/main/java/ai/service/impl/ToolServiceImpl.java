package ai.service.impl;

import ai.model.enums.ChatEventTypeEnum;
import ai.model.vo.ChatEventVO;
import ai.service.ToolService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ToolServiceImpl implements ToolService {
    @Resource(name = "toolClient")
    private ChatClient chatClient;
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

    private final static String  OUTPUT_STATUS = "OUTPUT_STATUS";
    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder()
            .eventType(ChatEventTypeEnum.STOP.getValue())
            .build();

//    /**
//     * chat
//     *
//     * @param question  问题
//     * @param sessionId 会话id
//     * @return 回答内容
//     */
//
//    public Flux<ChatEventVO> chat(String question, String sessionId) {
//        sessionService.updateTitle(sessionId,question);
//        // (1)大模型输出内容的缓存器，用于在输出中断后的数据存储
//        var outputBuilder = new StringBuilder();
//        //会话id-->转sessionId
//        var conversationId = ChatService.getConversationId(sessionId);
//        // 生成请求id
//        var requestId = IdUtil.fastSimpleUUID();
//        //控制是否stop
//        var outputHash = stringRedisTemplate.boundHashOps(OUTPUT_STATUS);
//        return toolClient.prompt()
//                .user(question)
//                .advisors(advisorSpec -> advisorSpec
//                        //会话记忆
//                        .param(ChatMemory.CONVERSATION_ID, conversationId))
//                .system(promptSystemSpec -> promptSystemSpec
//                        //系统role
//                        .text(systemPromptConfig.getChatSystemMessage().get())
//                        //param = 参数
//                        .param("now", LocalDateTime.now())
//                )
//                .toolContext(Map.of(Constant.REQUEST_ID, requestId)) //通过工具上下文传递参数
//                .stream()
//                .chatResponse()
//                // 第一次输出内容时执行
//                // 出现异常时，删除标识
//                // 完成时执行，删除标识
//                .doFirst(() -> outputHash.put(sessionId, "true"))  // 将布尔值转换为字符串存入 Redis
//                .doOnError(throwable -> outputHash.delete(sessionId))
//                .doOnComplete(() -> outputHash.delete(sessionId))
//                //(2)stop时仍然输出，当输出被取消时，保存输出的内容到历史记录中
//                .doOnCancel(() -> {
//                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
//                })
//                //控制是否继续
//                .takeWhile(chatResponse -> outputHash.get(sessionId) != null )
//
//                .map(chatResponse -> {
//                    // 对于响应结果进行处理，如果是最后一条数据，就把此次消息id放到内存中
//                    // 主要用于存储消息数据到 redis中，可以根据消息di获取的请求id，再通过请求id就可以获取到参数列表了
//                    // 从而解决，在历史聊天记录中没有外参数的问题
//                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
//                    if (StrUtil.equals(Constant.STOP, finishReason)) {
//                        var messageId = chatResponse.getMetadata().getId();
//                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
//                    }
//                    String response = chatResponse.getResult().getOutput().getText();
//                    // (3)追加到输出内容中
//                    outputBuilder.append(response);
//                    ChatEventVO chatEventVO = ChatEventVO.builder()
//                            .eventData(response)
//                            .eventType(ChatEventTypeEnum.DATA.getValue())
//                            .build();
//                    return chatEventVO;})
//
//                .concatWith(Flux.defer(() -> {
//                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
//                    var map = ToolResultHolder.get(requestId);
//                    if (!map.isEmpty()) {
//                        ToolResultHolder.remove(requestId); // 清除参数列表
//                        // 响应给前端的参数数据
//                        var chatEventVO = ChatEventVO.builder()
//                                .eventData(map)
//                                .eventType(ChatEventTypeEnum.PARAM.getValue())
//                                .build();
//                        return Flux.just(chatEventVO, STOP_EVENT);
//                    }
//                    return Flux.just(STOP_EVENT);
//                }));
//    }
//    /**
//     * 保存停止输出的记录
//     *
//     * @param conversationId 会话id
//     * @param content        大模型输出的内容
//     */
//    private void saveStopHistoryRecord(String conversationId, String content) {
//        chatMemory.add(conversationId, new AssistantMessage(content));
//    }
//
//    /**
//     * 停止生成
//     *
//     * @param sessionId 会话id
//     */
//    @Override
//    public void stop(String sessionId) {
//        var outputHash = stringRedisTemplate.boundHashOps(OUTPUT_STATUS);
//        // 移除标记
//        outputHash.delete(sessionId);
//    }
}