package ai.start.controller;

import ai.model.entity.Session;
import ai.model.vo.MessageVO;
import ai.model.vo.SessionTitleVO;
import ai.model.vo.SessionVO;
import ai.service.session.SessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.constant.ErrorConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;
    @PostMapping
    public Result startSession() {
        SessionVO sessionVO = sessionService.createSession();
        if (sessionVO == null) {
            return Result.error(ErrorConstant.AI_SESSION_ERROR);
        }
        return Result.success(sessionVO);
    }

    @GetMapping("/all")
    public Result getId(){
        List<Session> sessionList = sessionService.list(new LambdaQueryWrapper<Session>().select(Session::getSessionId));
        List<String> ids = sessionList.stream()
                .map(id -> id.getSessionId())
                .toList();
        return Result.success(ids);
    }

    /**
     * 查询单个历史对话详情
     *
     * @return 对话记录列表
     */
    @GetMapping("/{sessionId}")
    public List<MessageVO> queryBySessionId(@PathVariable("sessionId") String sessionId) {
        return sessionService.queryBySessionId(sessionId);
    }

    /**
     * 查询历史会话列表
     */
    @GetMapping("/history")
    public Result<Map<String, List<SessionTitleVO>>> queryHistorySession(){
        Map<String, List<SessionTitleVO>> map = sessionService.queryHistorySession();
        return Result.success(map);
    }
    /**
     * 更新历史会话标题
     *
     * @param sessionId 会话id
     * @param title     标题
     */
    @PutMapping("/title")
    public Result updateTitle(String sessionId, String title){
        sessionService.updateSessionTitle(sessionId, title);
        return Result.success(sessionId);
    }
    /**
     * 删除历史会话
     *
     * @param sessionId 会话ID
     */
    @DeleteMapping
    public Result deleteHistorySession(@RequestParam String sessionId){
        sessionService.deleteHistorySession(sessionId);
        return Result.success(sessionId);
    }
}
