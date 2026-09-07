package start.controller.websocket;

import lombok.extern.slf4j.Slf4j;
import model.entity.Flower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import service.FlowerService;


import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private FlowerService flowerService;
    /**
     * 3小时一次检查鲜花保质期, websocket发给admin店长端
     */
//    @Scheduled 使用 6 字段 cron 表达式（秒 分 时 日 月 周）
//  1	秒	0-59	*任意 / */n每隔 n 秒
//  2	分	0-59	*任意 / */n每隔 n 分钟
//  3	时	0-23	*任意小时
//  4	日	1-31	*任意日期
//  5	月	1-12	*任意月份
//  6	周	1 (周日)-7 (周六)	? 不指定（和日互斥）
    @Scheduled(cron = "0 0 3 * * ?")
    public void processTimeout(){
        List<Flower> flowerList = flowerService
                .lambdaQuery()
                .orderByAsc(Flower::getUpdateTime)
                .list();
        LocalDateTime now = LocalDateTime.now();
        for (Flower flower : flowerList) {
            if (flower.getUpdateTime().plusHours(24).isBefore(now)) {
                log.info("鲜花需要被及时处理" + flower.getId());
            }
        }
    }
}
