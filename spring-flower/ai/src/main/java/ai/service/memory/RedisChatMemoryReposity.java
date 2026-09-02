//package ai.service.memory;
//
//import cn.hutool.core.collection.CollStreamUtil;
//import cn.hutool.core.lang.Assert;
//import cn.hutool.core.util.StrUtil;
//import org.springframework.ai.chat.memory.ChatMemoryRepository;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//
//import java.util.List;
//import java.util.Set;
//
//@Service
//@Profile("redis")
//public class RedisChatMemoryReposity implements ChatMemoryRepository {
//    private static final String DEFAULT_PREFIX = "chat:";
//    private final String prefix;
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    public RedisChatMemoryReposity() {
//        this(DEFAULT_PREFIX);
//    }
//    public RedisChatMemoryReposity(String prefix) {
//        this.prefix = prefix;
//    }
//
//    @Override
//    public List<String> findConversationIds() {
//        Set<String> keys = this.stringRedisTemplate.keys(this.prefix + "*");
//        if (null == keys) {
//            return List.of();
//        }
//        return keys.stream()
//                .map(key -> StrUtil.replace(key, this.prefix, ""))
//                .toList();
//    }
//
//    @Override
//    public List<Message> findByConversationId(String conversationId) {
//        // 生成Redis键名用于存储会话消息
//        var redisKey = this.getKey(conversationId);
//        // 获取Redis列表操作对象
//        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
//
//        // 从Redis列表中获取所有的数据
//        var messages = listOps.range(0, -1);
//        // 将Redis返回的字符串列表转换为Message对象列表
//        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
//    }
//
//    @Override
//    public void saveAll(String conversationId, List<Message> messages) {
//        Assert.notEmpty(messages, "消息列表不能为空");
//        var redisKey = this.getKey(conversationId);
//        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
//        // 保存数据时，会传入全部的消息数据，包括之前的数据，所以需要先删除之前的数据，再添加新的数据
//        this.deleteByConversationId(conversationId);
//        // 将消息序列化并添加到Redis列表的右侧
//        messages.forEach(message -> listOps.rightPush(MessageUtil.toJson(message, conversationId)));
//    }
//
//    @Override
//    public void deleteByConversationId(String conversationId) {
//        var redisKey = this.getKey(conversationId);
//        this.stringRedisTemplate.delete(redisKey);
//    }
//    private String getKey(String conversationId) {
//        return this.prefix+conversationId;
//    }
//}
