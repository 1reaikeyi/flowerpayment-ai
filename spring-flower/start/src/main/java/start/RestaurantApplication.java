package start;

import common.properties.AliOssProperties;
import common.properties.JwtProperties;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;

// 日志配置
@Slf4j
//主程序入口
@SpringBootApplication
// 扫描bean组件
@ComponentScan(basePackages = {"common","service","start"})
// 扫描mapper接口
@MapperScan("mapper")
//aop
@EnableAspectJAutoProxy(proxyTargetClass = true)
// 开启spring-cache缓存
@EnableCaching
// 开启事务管理
@EnableTransactionManagement
// 开启定时任务
@EnableScheduling
// 开启配置属性
@EnableConfigurationProperties({AliOssProperties.class, JwtProperties.class})
@CrossOrigin
public class RestaurantApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantApplication.class, args);
        log.info("--匹配成功");
    }
}