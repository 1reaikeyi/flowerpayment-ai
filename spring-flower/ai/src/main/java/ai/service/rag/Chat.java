package ai.service.rag;

import ai.model.vo.ChatEventVO;
import reactor.core.publisher.Flux;

public interface Chat {
    /**
     * 获取对话id，
     *
     * @param sessionId 会话id
     * @return 对话id
     */
    static String getConversationId(String sessionId) {
        return sessionId;
    }

    /**
     * chat
     *
     * @param question  问题
     * @param sessionId 会话id
     * @return 回答内容
     */
    Flux<ChatEventVO> chat(String question, String sessionId);
    /**
     * 停止生成
     *
     * @param sessionId 会话id
     */
    void stop(String sessionId);

}

