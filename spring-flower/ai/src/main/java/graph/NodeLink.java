package graph;


import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class NodeLink {
    @Autowired
    private VisualFunction visualFunction;
    @Autowired
    private ToolFunction toolFunction;

    @Bean("see")
    public CompiledGraph toSee() {
        KeyStrategyFactory strategyFactory = new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {
                return Map.of(
                        "visualResult", new ReplaceStrategy(),
                        "toolResult", new ReplaceStrategy()
                );
            }
        };
        StateGraph graph = new StateGraph("see",strategyFactory);
        //节点
        try {
            graph.addNode("node1", AsyncNodeAction.node_async(visualFunction));
            graph.addNode("node2", AsyncNodeAction.node_async(toolFunction));
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
        //边
        try {
            graph.addEdge(StateGraph.START,"node1");
            graph.addEdge("node1","node2");
            graph.addEdge("node2",StateGraph.END);
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
        //flow
        CompiledGraph compiledGraph = null;
        try {
            compiledGraph = graph.compile();
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
        // UML 图生成与打印
        // 使用 PlantUML 格式生成图的可视化表示
        GraphRepresentation representation = graph.getGraph(GraphRepresentation.Type.PLANTUML, "takeout Flow");
        // 打印 UML 图内容到控制台
        System.out.println("\n=== English Flow UML Diagram ===");
        System.out.println(representation.content());
        System.out.println("==================================\n");
        
        return compiledGraph;
    }
}
