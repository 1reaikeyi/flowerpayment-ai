package ai.service.impl;

import ai.common.SessionProperties;
import ai.mapper.SessionMapper;
import ai.model.entity.Session;
import ai.model.vo.MessageVO;
import ai.model.vo.SessionTitleVO;
import ai.model.vo.SessionVO;
import ai.service.SessionService;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    @Autowired
    private SessionProperties sessionProperties;
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
     *
     * @param chatId
     * @return
     */
    @Override
    public List<MessageVO> queryBySessionId(String chatId) {
        return List.of();
    }

    /**
     * 查询历史会话列表
     */
    @Override
    public Map<String, List<SessionTitleVO>> queryHistorySession() {
        return Map.of();
    }

    /**
     * 更新历史会话标题
     *
     * @param sessionId 会话id
     * @param title     标题
     */
    @Override
    public void updateTitle(String sessionId, String title) {

    }

    /**
     * 删除历史会话
     *
     * @param sessionId 会话ID
     */
    @Override
    public void deleteHistorySession(String sessionId) {

    }
}
