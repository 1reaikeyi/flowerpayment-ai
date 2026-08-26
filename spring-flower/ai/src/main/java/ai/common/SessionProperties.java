package ai.common;

import ai.model.vo.SessionVO;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Data
@Configuration
@ConfigurationProperties(prefix = "session")
public class SessionProperties {
    /**
     * AI助手的标题，用于显示助手的名称或身份。
     */
    private String title;

    /**
     * AI助手的描述，简要介绍助手的功能或特点。
     */
    private String describe;

    /**
     * 示例列表，包含一些使用助手的示例。
     */
    private List<SessionVO.Example> examples;
}
