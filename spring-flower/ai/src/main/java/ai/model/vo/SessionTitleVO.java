package ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionTitleVO {

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 会话标题
     */
    private String title;

    private LocalDateTime updateTime;

}
