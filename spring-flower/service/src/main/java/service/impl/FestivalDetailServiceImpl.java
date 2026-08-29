package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.constant.RedisPrefixConstant;
import common.exception.FlowerDetailFailedException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import mapper.FestivalDetailMapper;
import model.dto.FestivalDetailDTO;
import model.dto.FlowerDTO;
import model.entity.Festival;
import model.entity.FestivalDetail;
import model.entity.Flower;
import model.entity.FlowerDetail;
import model.vo.FestivalDetailVO;
import model.vo.FestivalVO;
import model.vo.FlowerDetailVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redisdata.LogicData;
import service.FestivalDetailService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FestivalDetailServiceImpl extends ServiceImpl<FestivalDetailMapper, FestivalDetail> implements FestivalDetailService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    
//    时间统一使用s结算
    private static final long FLASH_CACHE_TTL = 30L;
    private static final long NEED_FLASH_CACHE_TTL = FLASH_CACHE_TTL / 10;
    private static final long REDIS_EXIST_TTL = 86400L;
    private static final Random RANDOM = new Random();
    private ExecutorService festivalDetailExecutor;

    @PostConstruct
    public void init() {
        festivalDetailExecutor = new ThreadPoolExecutor(
                2, 2, 8,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(32),
                r -> {
                    Thread t = new Thread(r, "festivalDetail-handler");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        log.info("Festival 缓存重建线程池初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (festivalDetailExecutor != null && !festivalDetailExecutor.isShutdown()) {
            log.info("FestivalDetail 缓存重建线程池开始关闭...");
            festivalDetailExecutor.shutdown(); // 不再接受新任务，已提交的任务继续执行
            try {
                if (!festivalDetailExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("线程池未能在10秒内优雅关闭，执行 shutdownNow");
                    festivalDetailExecutor.shutdownNow(); // 强制中断
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                festivalDetailExecutor.shutdownNow();
            }
            log.info("FestivalDetail 缓存重建线程池已关闭");
        }
    }
    @Override
    public FestivalDetailDTO create(FestivalDetailDTO festivalDetailDTO) {
        FestivalDetail festivalDetail = BeanUtil.copyProperties(festivalDetailDTO, FestivalDetail.class);
        super.save(festivalDetail);
        FestivalDetailDTO dto = BeanUtil.copyProperties(festivalDetail, FestivalDetailDTO.class);
        return dto;
    }

    @Override
    public FestivalDetailVO readCache(Long id) {
        String key = RedisPrefixConstant.FESTIVALDETAIL_PREFIX + id;
        String value;
        try {
            value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                FestivalDetail festivalDetail = this.getCache(id);
                return festivalDetail != null ? BeanUtil.toBean(festivalDetail, FestivalDetailVO.class) : null;
            }
        } catch (Exception e) {
            log.info("Redis 宕机:{}", e.getMessage());
            // Redis 不可用：降级直查数据库
            FestivalDetail festivalDetail = this.getCache(id);
            return festivalDetail != null ? BeanUtil.toBean(festivalDetail, FestivalDetailVO.class) : null;
        }
        LogicData logicData = new LogicData();
        try {
            logicData = JSONUtil.toBean(value, LogicData.class);
        } catch (Exception e) {
            log.info("json解析失败:{}", e.getMessage());
            // Redis 不可用：降级直查数据库
            FestivalDetail festivalDetail = this.getCache(id);
            return festivalDetail != null ? BeanUtil.toBean(festivalDetail, FestivalDetailVO.class) : null;
        }
        if(logicData.getData() == null){
            return null;
        }
        FestivalDetailVO old = null;
        if (logicData.getData() == null){
            return old;
        }
        if (logicData.getData() != null){
            old = BeanUtil.toBean(logicData.getData(), FestivalDetailVO.class);
        }
        if (logicData.getExpireTime().isAfter(LocalDateTime.now())){
            long remainSec = Duration.between(LocalDateTime.now(), logicData.getExpireTime()).getSeconds();
            if (remainSec < NEED_FLASH_CACHE_TTL) {
                logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL));
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(logicData),
                        REDIS_EXIST_TTL, TimeUnit.SECONDS);
            }
            log.info("festivalDetail缓存没有过期------------");
            return BeanUtil.toBean(logicData.getData(), FestivalDetailVO.class);
        }
        log.info("festivalDetail缓存过期------------");
        getRedis(id);
        return old;
    }
    private FestivalDetail getCache(Long id) {
        String lockKey = "festivalDetail:lock:" + id;
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
                latestVal = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.FESTIVAL_PREFIX + id);
            } catch (Exception e) {
                log.info("Redis 宕机:{}", e.getMessage());
                latestVal = null;
            }
            //缓存已经被重建
            if(latestVal != null) {
                LogicData latestData = JSONUtil.toBean(latestVal, LogicData.class);
                if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
                    return latestData.getData() == null ? null
                            : BeanUtil.toBean(latestData.getData(), FestivalDetail.class);
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
    private FestivalDetail getMysql(Long id){
        //查询数据库
        FestivalDetail festivalDetail = super.getById(id);
        LogicData logicData = new LogicData();
        //缓存穿透
        if (festivalDetail == null) {
            logicData.setData(null);
            logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL +
                    (RANDOM.nextLong(-(FLASH_CACHE_TTL / 10), FLASH_CACHE_TTL / 10))));
            try {
                stringRedisTemplate.opsForValue().set(RedisPrefixConstant.FESTIVAL_PREFIX + id, JSONUtil.toJsonStr(logicData),
                        REDIS_EXIST_TTL, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.info("空值缓存写入失败{}", e.getMessage());
            }
            return null;
        }
        //写入缓存
        logicData.setData(festivalDetail);
        logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL +
                (RANDOM.nextLong(-(FLASH_CACHE_TTL / 10), FLASH_CACHE_TTL / 10))));
        try {
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.FESTIVAL_PREFIX + id, JSONUtil.toJsonStr(logicData),
                    REDIS_EXIST_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("缓存写入失败" + e.getMessage());
        }
        return festivalDetail;
    }
    private void getRedis(Long id) {
        festivalDetailExecutor.execute(() ->{
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
    public void updateCache(FestivalDetailDTO festivalDetailDTO) {
        if(festivalDetailDTO.getId() == null){
            throw new FlowerDetailFailedException(ErrorConstant.OPERATION_ERROR);
        }
        LambdaUpdateWrapper<FestivalDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FestivalDetail::getId,festivalDetailDTO.getId());
        if(festivalDetailDTO.getFlowerId() != null){
            updateWrapper.set(FestivalDetail::getFlowerId,festivalDetailDTO.getFlowerId());
        }
        if (festivalDetailDTO.getFestivalId() != null){
            updateWrapper.set(FestivalDetail::getFestivalId,festivalDetailDTO.getFestivalId());
        }
        if (StrUtil.isNotBlank(festivalDetailDTO.getSpecObject())){
            updateWrapper.set(FestivalDetail::getSpecObject,festivalDetailDTO.getSpecObject());
        }
        if (StrUtil.isNotBlank(festivalDetailDTO.getSpecOption())){
            updateWrapper.set(FestivalDetail::getSpecOption,festivalDetailDTO.getSpecOption());
        }
        super.update(updateWrapper);
        stringRedisTemplate.delete(RedisPrefixConstant.FESTIVALDETAIL_PREFIX + festivalDetailDTO.getId());
    }

    @Override
    public void deleteCache(List<Long> ids) {
        super.removeByIds(ids);
        for (Long id : ids) {
            stringRedisTemplate.delete(RedisPrefixConstant.FESTIVALDETAIL_PREFIX + id);
        }
    }
}
