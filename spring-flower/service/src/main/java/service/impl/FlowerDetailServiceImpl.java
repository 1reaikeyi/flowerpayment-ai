package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.constant.RedisPrefixConstant;
import common.exception.FlowerDetailFailedException;
import common.exception.FlowerFailedException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import mapper.FlowerDetailMapper;
import model.dto.FlowerDTO;
import model.dto.FlowerDetailDTO;
import model.entity.FestivalDetail;
import model.entity.FlowerDetail;
import model.vo.FlowerDetailVO;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redisdata.LogicData;
import service.FlowerDetailService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Service
@Slf4j
public class FlowerDetailServiceImpl extends ServiceImpl<FlowerDetailMapper, FlowerDetail> implements FlowerDetailService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    // 时间统一使用s结算
    private static final long FLASH_CACHE_TTL = 30L;
    private static final long NEED_FLASH_CACHE_TTL = FLASH_CACHE_TTL / 10;
    private static final long REDIS_EXIST_TTL = 86400L;
    private static final Random RANDOM = new Random();
    private ExecutorService flowerDetailExecutor;
    @PostConstruct
    public void init() {
        flowerDetailExecutor = new ThreadPoolExecutor(
                2,2,8,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(32),
                r -> {
                    Thread t = new Thread(r, "flowerDetail-handler");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        log.info("flowerDetail缓存重建线程池初始化完成");
    }
    @PreDestroy
    public void destroy() {
        if (flowerDetailExecutor != null && !flowerDetailExecutor.isShutdown()) {
            log.info("flowerDetail缓存重建线程池开始关闭...");
            flowerDetailExecutor.shutdown(); // 不再接受新任务，已提交的任务继续执行
            try {
                if (!flowerDetailExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("线程池未能在10秒内关闭，执行 shutdownNow");
                    flowerDetailExecutor.shutdownNow(); // 强制中断
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                flowerDetailExecutor.shutdownNow();
            }
            log.info("flowerDetail缓存重建线程池已关闭");
        }
    }

    @Override
    public FlowerDetailDTO create(FlowerDetailDTO flowerDetailDTO) {
        FlowerDetail flowerDetail = BeanUtil.copyProperties(flowerDetailDTO, FlowerDetail.class);
        super.save(flowerDetail);
        FlowerDetailDTO dto = BeanUtil.copyProperties(flowerDetail, FlowerDetailDTO.class);
        return dto;
    }

    @Override
    public FlowerDetailVO readCache(Long id) {
        String key = RedisPrefixConstant.FLOWERDETAIL_PREFIX + id;
        LogicData logicData = new LogicData();
        String value;
        try {
//        1 缓存不存在
            value = stringRedisTemplate.opsForValue().get(key);
            if (value == null){
                FlowerDetail flowerDetail = this.getCache(id);
                return flowerDetail != null ? BeanUtil.toBean(flowerDetail, FlowerDetailVO.class) : null;
            }
        } catch (Exception e) {
            log.info("Redis 宕机:{}", e.getMessage());
            // Redis 不可用：降级直查数据库
            FlowerDetail flowerDetail = this.getCache(id);
            return flowerDetail != null ? BeanUtil.toBean(flowerDetail, FlowerDetailVO.class) : null;
        }
        //2 缓存存在
        try {
             logicData = JSONUtil.toBean(value, LogicData.class);
        } catch (Exception e) {
            log.info("json 解析失败:{}", e.getMessage());
            FlowerDetail flowerDetail = this.getCache(id);
            return flowerDetail != null ? BeanUtil.toBean(flowerDetail, FlowerDetailVO.class) : null;
        }
        FlowerDetailVO old = null;
        if (logicData.getData() == null){
            //防止穿透
            return old;
        }
        if (logicData.getData() != null){
            old = BeanUtil.toBean(logicData.getData(), FlowerDetailVO.class);
        }

        if (logicData.getExpireTime().isAfter(LocalDateTime.now())){
            long remainSec = Duration.between(LocalDateTime.now(), logicData.getExpireTime()).getSeconds();
            if (remainSec < NEED_FLASH_CACHE_TTL) {
                logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL));
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(logicData),
                        REDIS_EXIST_TTL, TimeUnit.SECONDS);
            }
            log.info("flowerDetail缓存没有过期------------");
            return BeanUtil.toBean(logicData.getData(), FlowerDetailVO.class);
        }
        //        3逻辑过期：重建缓存
        log.info("flowerDetail缓存过期-------------");
        getRedis(id);
        return old;
    }

    private FlowerDetail getCache(Long id) {
        String key = "flowerDetail:lock:" + id;
        RLock lock = redissonClient.getLock(key);
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
            if(latestVal != null) {
                LogicData latestData = JSONUtil.toBean(latestVal, LogicData.class);
                if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
                    return latestData.getData() == null ? null
                            : BeanUtil.toBean(latestData.getData(), FlowerDetail.class);
                }
            }
            //缓存重建失败
            return getMysql(id);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private FlowerDetail getMysql(Long id) {
        LogicData logicData = new LogicData();
        FlowerDetail flowerDetail = super.getById(id);
        if (flowerDetail == null) {
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
        logicData.setData(flowerDetail);
        logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL+
                (RANDOM.nextLong( -(FLASH_CACHE_TTL / 10),FLASH_CACHE_TTL / 10))));
        try {
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.FLOWER_PREFIX+ id, JSONUtil.toJsonStr(logicData),
                    REDIS_EXIST_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("空值缓存写入失败{}", e.getMessage());
        }
        return flowerDetail;
    }

    private void getRedis(Long id) {
        flowerDetailExecutor.submit(() -> {
            try {
                getCache(id);
            } catch (Exception e) {
                log.error("异步刷新缓存失败, id={}, 删除key", id, e);
                try {
                    stringRedisTemplate.delete(RedisPrefixConstant.FLOWER_PREFIX + id);
                } catch (Exception ex) {
                    log.error("删除key失败, id={}", id, ex);
                }
            }
        });
    }
    @Override
    public void updateCache(FlowerDetailDTO flowerDetailDTO) {
        if(flowerDetailDTO.getId() == null){
            throw new FlowerDetailFailedException(ErrorConstant.OPERATION_ERROR);
        }
        LambdaUpdateWrapper<FlowerDetail> updateWrapper = new LambdaUpdateWrapper<FlowerDetail>();
        updateWrapper.eq(FlowerDetail::getId,flowerDetailDTO.getId());
        if(flowerDetailDTO.getFlowerId() != null){
            updateWrapper.set(FlowerDetail::getFlowerId,flowerDetailDTO.getFlowerId());
        }
        if (StrUtil.isNotBlank(flowerDetailDTO.getSpecObject())){
            updateWrapper.set(FlowerDetail::getSpecObject,flowerDetailDTO.getSpecObject());
        }
        if (StrUtil.isNotBlank(flowerDetailDTO.getSpecOptions())){
            updateWrapper.set(FlowerDetail::getSpecOptions,flowerDetailDTO.getSpecOptions());
        }
        super.update(updateWrapper);
        stringRedisTemplate.delete(RedisPrefixConstant.FLOWERDETAIL_PREFIX + flowerDetailDTO.getId());
    }

    @Override
    public void deleteCache(List<Long> ids) {
        super.removeByIds(ids);
        for (Long id : ids) {
            stringRedisTemplate.delete(RedisPrefixConstant.FLOWERDETAIL_PREFIX + id);
        }
    }
}
