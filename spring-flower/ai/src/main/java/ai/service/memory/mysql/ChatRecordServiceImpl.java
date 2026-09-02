package ai.service.memory.mysql;

import ai.mapper.ChatRecordMapper;
import ai.model.entity.ChatRecord;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord> implements ChatRecordService {
}
