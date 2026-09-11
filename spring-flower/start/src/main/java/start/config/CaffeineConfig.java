package start.config;

import model.entity.FestivalDetail;
import model.entity.FlowerDetail;
import model.vo.FestivalDetailVO;
import model.vo.FlowerDetailVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String, Object> flowerCache() {
        return Caffeine.newBuilder()
                .initialCapacity(32)
                .maximumSize(64)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, FlowerDetailVO> flowerDetailCache() {
        return Caffeine.newBuilder()
                .initialCapacity(32)
                .maximumSize(64)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, FestivalDetailVO> festivalDetailCache(){
        return Caffeine.newBuilder()
                .initialCapacity(32)
                .maximumSize(64)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

}
