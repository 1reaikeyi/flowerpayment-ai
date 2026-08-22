package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.RedisPrefixConstant;
import lombok.extern.slf4j.Slf4j;
import mapper.FlowerMapper;
import model.dto.FlowerDTO;
import model.dto.FlowerPageDTO;
import model.entity.Flower;
import model.vo.FlowerVO;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redisdata.LogicData;
import service.FlowerService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Service
@Slf4j
public class FlowerServiceImpl extends ServiceImpl<FlowerMapper, Flower> implements FlowerService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    //时间统一使用s结算
    private static final long FLASH_CACHE_TTL = 30L;
    private static final long NEED_FLASH_CACHE_TTL = FLASH_CACHE_TTL / 10;
    private static final long REDIS_EXIST_TTL = 86400L;
    private static final Random RANDOM = new Random();
    private static final ExecutorService FLOWER_EXECUTOR = new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), // 有界队列，防止无限堆积
            r -> {
                Thread t = new Thread (r, "read-flower-handler");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满了，交给调用线程执行，不丢弃任务
    );

    @Override
    public FlowerVO readCache(Long id) {
        String key = RedisPrefixConstant.FLOWER_PREFIX + id;
        String value;

        try {
            value = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.info("Redis 宕机:{}", e.getMessage());
            // Redis 不可用：降级直查数据库
            Flower flower = this.getCache(id);
            return flower != null ? BeanUtil.toBean(flower, FlowerVO.class) : null;
        }
//        1缓存不存在
        if (value == null){
            Flower flower = this.getCache(id);
            return flower != null ? BeanUtil.toBean(flower, FlowerVO.class) : null;
        }
//        2缓存存在
        LogicData logicData = new LogicData();
        try {
            logicData = JSONUtil.toBean(value, LogicData.class);
        } catch (Exception e) {
            log.info("json 解析失败:{}", e.getMessage());
            //
            Flower flower = this.getCache(id);
            return flower != null ? BeanUtil.toBean(flower, FlowerVO.class) : null;
        }

        FlowerVO old = null;
        if (logicData.getData() == null) {
            //防止穿透
            return old;
        }
        if (logicData.getData() != null){
            //拿到旧数据
            old = BeanUtil.toBean(logicData.getData(), FlowerVO.class);
        }

        if (logicData.getExpireTime().isAfter(LocalDateTime.now())){
            long remainSec = Duration.between(LocalDateTime.now(), logicData.getExpireTime()).getSeconds();
            if (remainSec < NEED_FLASH_CACHE_TTL) {
                logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL));
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(logicData),
                            REDIS_EXIST_TTL, TimeUnit.SECONDS);
            }
            log.info("flower缓存没有过期------------");
            return BeanUtil.toBean(logicData.getData(), FlowerVO.class);
        }
        //        2.2逻辑过期：返回旧数据，异步重建缓存
        try {
            log.info("flower缓存过期-------------");
            this.getRedis(id);
        } catch (Exception ignore) {
            log.info("异步刷新失败:{}", ignore.getMessage());
        }
        return old;
    }

    private Flower getCache(Long id){
        String lockKey = "flower:lock:" + id;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("Redis 宕机:{}", e.getMessage());
            return getMysql(id);
        }
        try {
            if (!locked) {
                return null;
            }

            // 先检查：等待期间其他线程可能已重建缓存
            String latestVal;
            try {
                latestVal = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.FLOWER_PREFIX + id);
            } catch (Exception e) {
                log.info("Redis 宕机:{}", e.getMessage());
                latestVal = null;
            }
            //缓存已经被重建
            try {
                LogicData latestData = JSONUtil.toBean(latestVal, LogicData.class);
                if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
                    return latestData.getData() == null ? null
                            : BeanUtil.toBean(latestData.getData(), Flower.class);
                }
            } catch (Exception parseEx) {
                log.warn("双重检查缓存数据损坏，重新查 DB, key={}",
                        RedisPrefixConstant.FLOWER_PREFIX + id, parseEx);
            }
            //缓存重建失败
            return getMysql(id);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Flower getMysql(Long id) {
        //查询数据库
        Flower flower = super.getById(id);
        LogicData logicData = new LogicData();
        //缓存穿透
        if (flower == null) {
            logicData.setData(null);
            logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL+
                    (RANDOM.nextLong( -(FLASH_CACHE_TTL / 10),FLASH_CACHE_TTL / 10))));
            try {
                stringRedisTemplate.opsForValue().set(RedisPrefixConstant.FLOWER_PREFIX+ id, JSONUtil.toJsonStr(logicData),
                        REDIS_EXIST_TTL, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.info("空值缓存写入失败{}", e.getMessage());
            }
            return null;
        }
        //写入缓存
        logicData.setData(flower);
        logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL+
                (RANDOM.nextLong( -(FLASH_CACHE_TTL / 10),FLASH_CACHE_TTL / 10))));
        try {
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.FLOWER_PREFIX+ id, JSONUtil.toJsonStr(logicData),
                    REDIS_EXIST_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("缓存写入失败" + e.getMessage());
        }
        return flower;

    }
    private void getRedis(Long id) {
        FLOWER_EXECUTOR.execute(() -> {
            log.info("缓存过期，EXECUTOR处理-------------");
            getCache(id);
        });

    }
    @Override
    public void updateCache(FlowerDTO flowerDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }

    @Override
    public List<FlowerVO> readPage(FlowerPageDTO flowerPageDTO) {
        return List.of();
    }

    @Override
    public FlowerDTO create(FlowerDTO flowerDTO) {
        return null;
    }


}
