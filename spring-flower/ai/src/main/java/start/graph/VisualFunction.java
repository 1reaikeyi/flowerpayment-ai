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
import service.VisualService;

import java.net.URI;
import java.util.Map;


@Service
@Slf4j
public class VisualFunction implements NodeAction {

    @Autowired
    private VisualService visualService;
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        return Map.of();
    }

}