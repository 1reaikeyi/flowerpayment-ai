package ai.service.memory.mysql;

import ai.model.entity.ChatRecord;
import ai.service.memory.MessageUtil;
import cn.hutool.core.collection.CollStreamUtil;
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
        var chatRecordList = chatRecordService.lambdaQuery()
                .select(ChatRecord::getSessionId)
                .list();
        List<String> conversationIdList = chatRecordList.stream()
                .map(session -> session.getSessionId())
                .toList();
        return conversationIdList;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        var chatRecordList = chatRecordService.lambdaQuery()
                .eq(ChatRecord::getSessionId, conversationId)
                .orderByAsc(ChatRecord::getCreateTime)
                .list();
        return CollStreamUtil.toList(chatRecordList, chatRecord -> MessageUtil.toMessage(chatRecord.getData()));
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 先删除原有数据
        this.deleteByConversationId(conversationId);
        // 批量保存数据到数据库
        var chatRecordList = CollStreamUtil.toList(messages, message -> ChatRecord.builder()
                .data(MessageUtil.toJson(message, conversationId))
                .sessionId(conversationId)
                .build());
        this.chatRecordService.saveBatch(chatRecordList);
    }

    @Override
    public void deleteByConversationId(String sessionId) {
        chatRecordService.remove(new LambdaUpdateWrapper<ChatRecord>()
                .eq(ChatRecord::getSessionId, sessionId));
    }
}
