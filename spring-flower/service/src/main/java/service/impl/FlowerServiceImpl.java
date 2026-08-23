package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.constant.RedisPrefixConstant;
import common.exception.FlowerFailedException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import mapper.FlowerMapper;
import model.dto.FlowerDTO;
import model.dto.FlowerPageDTO;
import model.entity.Flower;
import model.entity.FlowerCategory;
import model.vo.FlowerCategoryVO;
import model.vo.FlowerVO;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redisdata.LogicData;
import service.FlowerService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class FlowerServiceImpl extends ServiceImpl<FlowerMapper, Flower> implements FlowerService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    // 时间统一使用s结算
    private static final long FLASH_CACHE_TTL = 30L;
    private static final long NEED_FLASH_CACHE_TTL = FLASH_CACHE_TTL / 10;
    private static final long REDIS_EXIST_TTL = 86400L;
    private static final Random RANDOM = new Random();
    private ExecutorService flowerExecutor;
    @PostConstruct
    public void init() {
        flowerExecutor = new ThreadPoolExecutor(
               2,2,8,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(32),
                r -> {
                    Thread t = new Thread(r, "flower-handler");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        log.info("Flower 缓存重建线程池初始化完成");
    }
    @PreDestroy
    public void destroy() {
        if (flowerExecutor != null && !flowerExecutor.isShutdown()) {
            log.info("Flower 缓存重建线程池开始关闭...");
            flowerExecutor.shutdown(); // 不再接受新任务，已提交的任务继续执行
            try {
                if (!flowerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("线程池未能在10秒内优雅关闭，执行 shutdownNow");
                    flowerExecutor.shutdownNow(); // 强制中断
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                flowerExecutor.shutdownNow();
            }
            log.info("Flower 缓存重建线程池已关闭");
        }
    }
    private final ConcurrentHashMap<Long, Boolean> check = new ConcurrentHashMap<>();

    @Override
    public FlowerVO readCache(Long id) {
        String key = RedisPrefixConstant.FLOWER_PREFIX + id;
        String value;

        try {
//        1 缓存不存在
            value = stringRedisTemplate.opsForValue().get(key);
            if (value == null){
                Flower flower = this.getCache(id);
                return flower != null ? BeanUtil.toBean(flower, FlowerVO.class) : null;
            }
        } catch (Exception e) {
            log.info("Redis 宕机:{}", e.getMessage());
            // Redis 不可用：降级直查数据库
            Flower flower = this.getCache(id);
            return flower != null ? BeanUtil.toBean(flower, FlowerVO.class) : null;
        }
//        2 缓存存在
        LogicData logicData = new LogicData();
        try {
            logicData = JSONUtil.toBean(value, LogicData.class);
        } catch (Exception e) {
            log.info("json 解析失败:{}", e.getMessage());
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
            //旧数据暂时不用,稳定之后再使用
            old = null;
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
        //        3逻辑过期：重建缓存
        log.info("flower缓存过期-------------");
        Flower flower = getRedis(id);
        return flower != null ? BeanUtil.toBean(flower, FlowerVO.class) : null;
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
    private Flower getRedis(Long id) {
        if (check.putIfAbsent(id, Boolean.TRUE) != null) {
            // 已经有线程在重建
            return null;
        }
        Future<Flower> future = null;
        try {
            future = flowerExecutor.submit(() -> {
                try {
                    return getCache(id);
                } catch (Exception e) {
                    log.error("异步刷新缓存失败, id={}, 删除key", id, e);
                    try {
                        stringRedisTemplate.delete(RedisPrefixConstant.FLOWER_PREFIX + id);
                    } catch (Exception ex) {
                        log.error("删除key失败, id={}", id, ex);
                    }
                    return null;
                } finally {
                    check.remove(id); // ★ 无论成败，解除防重标记
                }
            });

            return future.get(3, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            // 超时：任务还在后台跑，check 标记等任务结束由 finally 清理
            log.warn("缓存重建超时(3s), id={}, 返回null让下次请求重试", id);
            return null;
        } catch (Exception e) {
            log.error("获取 Flower 失败, id={}", id, e);
            return null;
        } finally {
            // ★ future == null 说明 submit() 都没成功（RejectedExecutionException），
            //   任务里的 finally 不会执行，所以这里兜底清理
            if (future == null) {
                check.remove(id);
            }
        }
    }

    @Override
    public void updateCache(FlowerDTO flowerDTO) {
        if(flowerDTO.getId() == null){
            throw new FlowerFailedException(ErrorConstant.OPERATION_ERROR);
        }
        LambdaUpdateWrapper<Flower> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Flower::getId, flowerDTO.getId());

        if(StrUtil.isNotBlank(flowerDTO.getName())){
            updateWrapper.set(Flower::getName, flowerDTO.getName());
        }
        if(StrUtil.isNotBlank(flowerDTO.getDescription())){
            updateWrapper.set(Flower::getDescription, flowerDTO.getDescription());
        }
        if(flowerDTO.getPrice() != null){
            updateWrapper.set(Flower::getPrice, flowerDTO.getPrice());
        }
        if(flowerDTO.getCategoryId() != null){
            updateWrapper.set(Flower::getCategoryId, flowerDTO.getCategoryId());
        }
        if(flowerDTO.getStatus() != null){
            updateWrapper.set(Flower::getStatus, flowerDTO.getStatus());
        }
        if(flowerDTO.getImage() != null){
            updateWrapper.set(Flower::getImage, flowerDTO.getImage());
        }
        if(StrUtil.isNotBlank(flowerDTO.getColor())){
            updateWrapper.set(Flower::getColor, flowerDTO.getColor());
        }
        super.update(updateWrapper);
    }

    @Override
    public void deleteCache(List<Long> ids) {
        super.removeByIds(ids);
        for (Long id : ids) {
            stringRedisTemplate.delete(RedisPrefixConstant.FLOWER_PREFIX + id);
        }

    }
    @Override
    public FlowerDTO create(FlowerDTO flowerDTO) {
        Flower flower = BeanUtil.copyProperties(flowerDTO, Flower.class);
        super.save(flower);
        FlowerDTO dto = BeanUtil.copyProperties(flower, FlowerDTO.class);
        return dto;
    }

    @Override
    public List<FlowerVO> readPage(FlowerPageDTO flowerPageDTO) {
        LambdaQueryWrapper<Flower> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(flowerPageDTO.getName() != null, Flower::getName, flowerPageDTO.getName());
        IPage page = new Page(flowerPageDTO.getPage(),flowerPageDTO.getPageSize());
        IPage<Flower> flowerIPage = super.page(page,queryWrapper);
        List<FlowerVO> voList = flowerIPage.getRecords().stream()
                .map(flower -> BeanUtil.copyProperties(flower, FlowerVO.class))
                .collect(Collectors.toList());
        return voList;
    }




}
