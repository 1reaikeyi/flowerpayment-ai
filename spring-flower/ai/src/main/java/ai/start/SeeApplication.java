package ai.start;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 排除 Spring Security 自动配置：ai 模块通过 common 间接引入了 security 依赖，
// 但 ai 模块独立启动时没有自定义 SecurityConfig，会导致所有请求被默认拦截返回 401
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import service.impl.FestivalDetailServiceImpl;
import service.impl.FestivalServiceImpl;
import service.impl.FlowerDetailServiceImpl;
import service.impl.FlowerServiceImpl;


@SpringBootApplication
@MapperScan("mapper")
@ComponentScan(basePackages = {"common","ai"})
@Import({FestivalServiceImpl.class, FlowerServiceImpl.class, FestivalDetailServiceImpl.class, FlowerDetailServiceImpl.class})
@Slf4j
public class SeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeeApplication.class, args);
        log.info(">>>ai");
    }

}
