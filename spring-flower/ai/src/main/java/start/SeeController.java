package start;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import graph.NodeLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class SeeController {
    @Autowired
    private NodeLink nodeLink;

    @PostMapping("/see")
    public Object flow(@RequestParam String question,
                       @RequestParam MultipartFile file) throws Exception {
        CompiledGraph compiledGraph = nodeLink.toSee();
        // 将文件转为 Base64 字符串再传入 state，避免 graph 框架无法处理 byte[]
        String fileBase64 = Base64.getEncoder().encodeToString(file.getBytes());
        return null;
    }
}
