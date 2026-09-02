package ai.service.memory.mysql;

import ai.model.entity.ChatRecord;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class MysqlChatMemoryRepository implements ChatMemoryRepository {
    @Autowired
    private ChatRecordService chatRecordService;

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    @Override
    public void deleteByConversationId(String sessionId) {
        chatRecordService.remove(new LambdaUpdateWrapper<ChatRecord>()
                .eq(ChatRecord::getSessionId, sessionId));
    }
}
