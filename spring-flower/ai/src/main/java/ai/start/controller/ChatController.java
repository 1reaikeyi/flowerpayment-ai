package ai.start.controller;

import ai.model.dto.ChatDTO;
import ai.model.vo.ChatEventVO;
import ai.service.rag.Chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("chat")
public class ChatController {
    @Autowired
    private Chat chatService;
    /**
     * 对话
     * @param chatDTO
     * @return
     */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        return chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }
    /**
     * stop_chat
     */
    @PostMapping("/stop")
    public void stop(@RequestParam String sessionId) {
        chatService.stop(sessionId);
    }

}
