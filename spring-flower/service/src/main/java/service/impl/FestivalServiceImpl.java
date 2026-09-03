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
import common.exception.FestivalFailedException;
import common.result.PageResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import mapper.FestivalMapper;
import model.dto.FestivalDTO;
import model.dto.FestivalPageDTO;
import model.entity.Festival;
import model.entity.FestivalDetail;
import model.entity.Flower;
import model.vo.EmployeeVO;
import model.vo.FestivalDetailVO;
import model.vo.FestivalVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redisdata.LogicData;
import service.FestivalDetailService;
import service.FestivalService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class FestivalServiceImpl extends ServiceImpl<FestivalMapper, Festival> implements FestivalService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private FestivalDetailService festivalDetailService;

    // 时间统一使用s结算
    private static final long FLASH_CACHE_TTL = 30L;
    private static final long NEED_FLASH_CACHE_TTL = FLASH_CACHE_TTL / 10;
    private static final long REDIS_EXIST_TTL = 86400L;
    private static final Random RANDOM = new Random();
    private ExecutorService festivalExecutor;

    @PostConstruct
    public void init() {
        festivalExecutor = new ThreadPoolExecutor(
                2, 2, 8,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(32),
                r -> {
                    Thread t = new Thread(r, "festival-handler");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        log.info("Festival 缓存重建线程池初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (festivalExecutor != null && !festivalExecutor.isShutdown()) {
            log.info("Festival 缓存重建线程池开始关闭...");
            festivalExecutor.shutdown(); // 不再接受新任务，已提交的任务继续执行
            try {
                if (!festivalExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("线程池未能在10秒内优雅关闭，执行 shutdownNow");
                    festivalExecutor.shutdownNow(); // 强制中断
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                festivalExecutor.shutdownNow();
            }
            log.info("Festival 缓存重建线程池已关闭");
        }
    }


    @Override
    public FestivalVO readCache(Long id) {
        String key = RedisPrefixConstant.FESTIVAL_PREFIX + id;
        String value;

        try {
//        1 缓存不存在
            value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                Festival festival = this.getCache(id);
                return festival != null ? BeanUtil.toBean(festival, FestivalVO.class) : null;
            }
        } catch (Exception e) {
            log.info("Redis 宕机:{}", e.getMessage());
            // Redis 不可用：降级直查数据库
            Festival festival = this.getCache(id);
            return festival != null ? BeanUtil.toBean(festival, FestivalVO.class) : null;
        }
//        2 缓存存在
        LogicData logicData = new LogicData();
        try {
            logicData = JSONUtil.toBean(value, LogicData.class);
        } catch (Exception e) {
            log.info("json 解析失败:{}", e.getMessage());
            Festival festival = this.getCache(id);
            return festival != null ? BeanUtil.toBean(festival, FestivalVO.class) : null;
        }

        FestivalVO old = null;
        if (logicData.getData() == null) {
            //防止穿透
            return old;
        }
        if (logicData.getData() != null) {
            //拿到旧数据
            old = BeanUtil.toBean(logicData.getData(), FestivalVO.class);
            //旧数据暂时不用，稳定之后再使用
            old = null;
        }

        if (logicData.getExpireTime().isAfter(LocalDateTime.now())) {
            long remainSec = Duration.between(LocalDateTime.now(), logicData.getExpireTime()).getSeconds();
            if (remainSec < NEED_FLASH_CACHE_TTL) {
                logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL));
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(logicData),
                        REDIS_EXIST_TTL, TimeUnit.SECONDS);
            }
            log.info("festival缓存没有过期------------");
            return BeanUtil.toBean(logicData.getData(), FestivalVO.class);
        }
        //        3逻辑过期：重建缓存
        log.info("festival缓存过期-------------");
        Festival festival = getRedis(id);
        return festival != null ? BeanUtil.toBean(festival, FestivalVO.class) : null;
    }

    private Festival getCache(Long id) {
        String lockKey = "festival:lock:" + id;
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
                            : BeanUtil.toBean(latestData.getData(), Festival.class);
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

    private Festival getMysql(Long id) {
        //查询数据库
        Festival festival = super.getById(id);
        LogicData logicData = new LogicData();
        //缓存穿透
        if (festival == null) {
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
        logicData.setData(festival);
        logicData.setExpireTime(LocalDateTime.now().plusSeconds(FLASH_CACHE_TTL +
                (RANDOM.nextLong(-(FLASH_CACHE_TTL / 10), FLASH_CACHE_TTL / 10))));
        try {
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.FESTIVAL_PREFIX + id, JSONUtil.toJsonStr(logicData),
                    REDIS_EXIST_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("缓存写入失败" + e.getMessage());
        }
        return festival;

    }

    private Festival getRedis(Long id) {
        Future<Festival> future = null;
        try {
            future = festivalExecutor.submit(() -> {
                try {
                    return getCache(id);
                } catch (Exception e) {
                    log.error("异步刷新缓存失败, id={}, 删除key", id, e);
                    try {
                        stringRedisTemplate.delete(RedisPrefixConstant.FESTIVAL_PREFIX + id);
                    } catch (Exception ex) {
                        log.error("删除key失败, id={}", id, ex);
                    }
                    return null;
                }
            });
            return future.get(3, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            // 阻塞超时
            log.info("缓存重建超时(3s), id={}, 返回null", id);
            return null;
        } catch (InterruptedException | ExecutionException e) {
            // 任务执行失败或被中断
            log.error("获取 Flower 失败, id={}", id, e);
            return null;
        }

    }

    @Override
    public void updateCache(FestivalDTO festivalDTO) {
        if (festivalDTO.getId() == null) {
            throw new FestivalFailedException(ErrorConstant.OPERATION_ERROR);
        }
        LambdaUpdateWrapper<Festival> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Festival::getId, festivalDTO.getId());

        if (StrUtil.isNotBlank(festivalDTO.getName())) {
            updateWrapper.set(Festival::getName, festivalDTO.getName());
        }
        if (StrUtil.isNotBlank(festivalDTO.getDescription())) {
            updateWrapper.set(Festival::getDescription, festivalDTO.getDescription());
        }
        if (festivalDTO.getPrice() != null) {
            updateWrapper.set(Festival::getPrice, festivalDTO.getPrice());
        }
        if (festivalDTO.getCategoryId() != null) {
            updateWrapper.set(Festival::getCategoryId, festivalDTO.getCategoryId());
        }
        if (festivalDTO.getStatus() != null) {
            updateWrapper.set(Festival::getStatus, festivalDTO.getStatus());
        }
        if (festivalDTO.getImage() != null) {
            updateWrapper.set(Festival::getImage, festivalDTO.getImage());
        }
        if (festivalDTO.getNumber() != null) {
            updateWrapper.set(Festival::getNumber, festivalDTO.getNumber());
        }
        super.update(updateWrapper);
        stringRedisTemplate.delete(RedisPrefixConstant.FESTIVAL_PREFIX + festivalDTO.getId());
    }

    @Override
    public void deleteCache(List<Long> ids) {
        super.removeByIds(ids);
        for (Long id : ids) {
            stringRedisTemplate.delete(RedisPrefixConstant.FESTIVAL_PREFIX + id);
        }

    }

    @Override
    public FestivalDTO create(FestivalDTO festivalDTO) {
        Festival festival = BeanUtil.copyProperties(festivalDTO, Festival.class);
        super.save(festival);
        FestivalDTO dto = BeanUtil.copyProperties(festival, FestivalDTO.class);
        return dto;
    }

    @Override
    public PageResult<FestivalVO> readPage(FestivalPageDTO festivalPageDTO) {
        LambdaQueryWrapper<Festival> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(festivalPageDTO.getName() != null, Festival::getName, festivalPageDTO.getName());
        IPage page = new Page(festivalPageDTO.getPage(), festivalPageDTO.getPageSize());
        IPage<Festival> festivalIPage = super.page(page, queryWrapper);
        List<FestivalVO> voList = festivalIPage.getRecords().stream()
                .map(festival ->BeanUtil.toBean(festival, FestivalVO.class))
                .collect(Collectors.toList());
        PageResult<FestivalVO> result = new PageResult<>();
        result.setTotal(festivalIPage.getTotal());
        result.setList(voList);                         // 当前页数据
        result.setPageNum(festivalIPage.getCurrent());  // 当前页码
        result.setPageSize(festivalIPage.getSize());    // 每页条数
        return result;
    }

    @Override
    public List<FestivalDetailVO> readFestivalDetail(Long id) {
        List<FestivalDetail> festivalList = festivalDetailService.lambdaQuery().eq(FestivalDetail::getFestivalId, id).list();
        List<FestivalDetailVO> festivalDetailVOList = festivalList.stream()
                .map(festivalDetailVO -> BeanUtil.toBean(festivalDetailVO, FestivalDetailVO.class) )
                .toList();
        return festivalDetailVOList;
    }

    @Override
    public List<FestivalDetailVO> readFlower(Long id) {
        List<FestivalDetail> festivalList = festivalDetailService.lambdaQuery().eq(FestivalDetail::getFlowerId, id).list();
        List<FestivalDetailVO> festivalDetailVOList = festivalList.stream()
                .map(festivalDetailVO -> BeanUtil.toBean(festivalDetailVO, FestivalDetailVO.class) )
                .toList();
        return festivalDetailVOList;
    }


}
