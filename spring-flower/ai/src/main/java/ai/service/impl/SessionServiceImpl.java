package ai.service.impl;

import ai.mapper.SessionMapper;
import ai.model.entity.Session;
import ai.model.vo.MessageVO;
import ai.model.vo.SessionTitleVO;
import ai.model.vo.SessionVO;
import ai.service.SessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    /**
     * 创建会话session
     *
     * @param num 热门问题的数量
     * @return 会话信息
     */
    @Override
    public SessionVO createSession(Long num) {
        return null;
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
