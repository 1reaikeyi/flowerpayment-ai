package start.controller.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 * id 规则：
 *   顾客端：user_{userId}
 *   商家端：admin_{adminId}
 */
@Component
@ServerEndpoint("/websocket/{id}")
public class WebSocketServer {

    // 存放所有在线会话对象，key 为连接 id（如 user_1、admin_1）
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        // 客户端建立连接时，把会话对象存入 Map 以便后续推送
        System.out.println("客户端：" + id + " 建立连接");
        sessionMap.put(id, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("id") String id) {
        System.out.println("收到来自客户端：" + id + " 的信息:" + message);

        // 顾客发送 reminder：催单请求，转发给所有商家
        if ("reminder".equals(message) && id.startsWith("user_")) {
            sendToAllAdmin("reminder");
        }

        // 商家发送 received：商家已收到催单，转发给所有顾客
        if ("received".equals(message) && id.startsWith("admin_")) {
            sendToAllUser("received");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {
        // 客户端断开连接时，从 Map 中移除会话
        System.out.println("连接断开:" + id);
        sessionMap.remove(id);
    }

    /**
     * 向所有商家端（id 以 admin_ 开头）推送消息
     *
     * @param message 要推送的消息内容
     */
    private void sendToAllAdmin(String message) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            // 只推送给商家端会话
            if (entry.getKey().startsWith("admin_")) {
                Session session = entry.getValue();
                try {
                    // 服务器向商家客户端发送催单消息
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向所有顾客端（id 以 user_ 开头）推送消息
     *
     * @param message 要推送的消息内容
     */
    private void sendToAllUser(String message) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            // 只推送给顾客端会话
            if (entry.getKey().startsWith("user_")) {
                Session session = entry.getValue();
                try {
                    // 服务器向顾客客户端发送商家已收到消息
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
