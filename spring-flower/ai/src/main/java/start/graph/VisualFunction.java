package start.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import service.VisualService;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class VisualFunction implements NodeAction {

    @Autowired
    private VisualService visualService;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String file = Optional.ofNullable(state.value("file"))
                .map(Object::toString)
                .orElse("视觉识别出错");

        Media media = new Media(MimeTypeUtils.IMAGE_JPEG, URI.create("data:image/jpeg;base64," + file));

        String result =  visualService.chat(media)
                .collectList()
                .timeout(Duration.ofSeconds(30))
                .blockOptional(Duration.ofSeconds(60))
                .toString();
        return Map.of("visualResult",result);
    }

}