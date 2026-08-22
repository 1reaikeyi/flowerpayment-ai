package start.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 构建 Redis 专用的 ObjectMapper：
     * 继承 Web 层 ObjectMapper 的日期格式化等配置，并设置所有属性可见，
     * 保证实体类字段序列化完整。
     */
    private ObjectMapper buildRedisObjectMapper(ObjectMapper webMapper) {
        ObjectMapper redisMapper = webMapper.copy();
        redisMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        return redisMapper;
    }

    /**
     * 统一构造 GenericJackson2JsonRedisSerializer：
     * RedisTemplate 与 RedisCacheManager 共用同一个序列化器实现，
     * 避免一边用 GenericJackson 一边用 Jackson2Json 导致读写格式不一致。
     *
     * GenericJackson2JsonRedisSerializer 内部特点：
     *   - 自带 LaissezFaireSubTypeValidator（宽松多态校验，不会因为包名不在白名单就不写类型）
     *   - activateDefaultTyping(NON_FINAL, WRAPPER_ARRAY)：用 ["类名", 实际内容] 格式包裹，
     *     对 List/Map 等 JSON 数组/对象集合，外层一定能带上类型信息，反序列化时不会丢失集合具体类型。
     */
    private GenericJackson2JsonRedisSerializer buildJsonSerializer(ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(buildRedisObjectMapper(objectMapper));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // key / hashKey 使用字符串序列化器，保证可读性
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        GenericJackson2JsonRedisSerializer jsonSerializer = buildJsonSerializer(objectMapper);
        // value / hashValue 使用统一 JSON 序列化器，带多态类型信息
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // Spring Cache 与 RedisTemplate 使用相同的 JSON 序列化器，读写格式 100% 一致，
        // 避免 A 写的缓存 B 读不出来的情况
        GenericJackson2JsonRedisSerializer jsonSerializer = buildJsonSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> cacheName)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .build();
    }
}
