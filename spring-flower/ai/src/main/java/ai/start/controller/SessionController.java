package ai.start.controller;

import ai.model.vo.SessionVO;
import ai.service.SessionService;
import common.constant.ErrorConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
