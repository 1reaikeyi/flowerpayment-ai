package ai.service.session;

import ai.common.SessionProperties;
import ai.mapper.SessionMapper;
import ai.model.entity.ChatRecord;
import ai.model.entity.Session;
import ai.model.enums.MessageTypeEnum;
import ai.model.vo.MessageVO;
import ai.model.vo.SessionTitleVO;
import ai.model.vo.SessionVO;
import ai.service.chat.ChatService;
import ai.service.memory.AssistantMessageUtil;
import ai.service.memory.mysql.ChatRecordService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    @Autowired
    private SessionProperties sessionProperties;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private ChatRecordService chatRecordService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建会话session
     *
     * @return 会话信息
     */
    @Override
    public SessionVO createSession() {
        String id = UUID.randomUUID(true).toString();
        LocalDateTime now = LocalDateTime.now();
        String sessionId = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        Session session = Session.builder()
                .id(Long.parseLong(id))
                .sessionId(sessionId)
                //先独立，后续合并到start,接入spring security
                .userId(0L)
                .build();
        SessionVO sessionVO = new SessionVO();
        List<SessionVO.Example> examples = sessionProperties.getExamples();
        List<SessionVO.Example> sessionExampleList = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            Random random = new Random();
            random.nextInt(i,examples.size());
            sessionExampleList.add(examples.get(i));
        }
        sessionVO.setExamples(sessionExampleList);
        sessionVO.setSessionId(sessionId);
        sessionVO.setTitle(sessionProperties.getTitle());
        sessionVO.setDescribe(sessionProperties.getDescribe());
        return sessionVO;
    }

    /**
     * 查询chat
     * @return
     */
    @Override
    public List<MessageVO> queryBySessionId(String sessionId) {
        // 根据session获取对话ID,从chatmemory中获取历史消息
        String conversationId = ChatService.getConversationId(sessionId);
        /**  Message
         * USER("user"),
         * ASSISTANT("assistant"),
         * SYSTEM("system"),
         * TOOL("tool");
         */
        List<Message> messageList = chatMemory.get(conversationId);
        // 过滤并转换消息列表
        return messageList.stream()
                // 过滤掉非用户消息和助手消息
                .filter(message -> message.getMessageType() == MessageType.ASSISTANT || message.getMessageType() == MessageType.USER)
                .map(message -> {
                    if (message instanceof AssistantMessageUtil) {
                        return MessageVO.builder()
                                .content(message.getText())
                                .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                                .params(((AssistantMessageUtil) message).getParams())
                                .build();
                    }
                    //if (message instanceof USERMessageUtil) {}
                    return MessageVO.builder()
                            .content(message.getText())
                            .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                            .build();
                })
                .toList();

    }

    /**
     * 查询历史会话列表
     */
    @Override
    public Map<String, List<SessionTitleVO>> queryHistorySession() {
        // 查询历史会话，限制返回条数
        List<Session> list = super.lambdaQuery()
                .orderByDesc(Session::getUpdateTime)
                .last("LIMIT 30")
                .list();

        if (list.isEmpty()) {
            log.info("No chat sessions found");
            return Map.of();
        }

        List<SessionTitleVO> chatSessionVOList = list.stream()
                .map(chat -> BeanUtil.toBean(chat,SessionTitleVO.class))
                .toList();
        final var TODAY = "当天";
        final var LAST_30_DAYS = "最近30天";
        final var LAST_YEAR = "最近1年";
        final var MORE_THAN_YEAR = "1年以上";

        // 当前时间
        var now = LocalDateTime.now().toLocalDate();

        // 按照更新时间分组
        return chatSessionVOList.stream()
                .collect(Collectors.groupingBy(vo -> {
                    // 计算两个日期之间的天数差
                    long between = Math.abs(ChronoUnit.DAYS.between(vo.getUpdateTime().toLocalDate(), now));
                    if (between == 0) {
                        return TODAY;
                    } else if (between <= 30) {
                        return LAST_30_DAYS;
                    } else if (between <= 365) {
                        return LAST_YEAR;
                    } else {
                        return MORE_THAN_YEAR;
                    }
                }));
    }

    /**
     * 更新历史会话标题
     *
     * @param sessionId 会话id
     * @param title     标题
     */
    @Override
    public void updateSessionTitle(String sessionId, String title) {
        List<Session> list = super.lambdaQuery()
                .eq(Session::getSessionId, sessionId)
                .list();
        if (list.isEmpty()) {
            return;
        }
        Session session= list.get(0);
        // 安全截取标题，避免长度不足时抛出异常
        session.setTitle(title.length() > 100 ? title.substring(0, 100) : title);
        this.updateById(session);
    }

    /**
     * 删除历史会话
     *
     * @param sessionId 会话ID
     */
    @Override
    public void deleteHistorySession(String sessionId) {
        //1删除mysql
        super.remove(new LambdaQueryWrapper<Session>()
                .eq(Session::getSessionId, sessionId));
        chatRecordService.remove(new LambdaQueryWrapper<ChatRecord>()
                .eq(ChatRecord::getSessionId, sessionId));
        //2删除缓存
        stringRedisTemplate.delete(sessionId);
    }
}
