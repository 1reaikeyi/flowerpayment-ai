package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.RedisPrefixConstant;
import lombok.extern.slf4j.Slf4j;
import mapper.FestivalMapper;
import model.dto.FestivalDTO;
import model.entity.Festival;
import model.entity.Flower;
import model.vo.FestivalVO;
import model.vo.FlowerVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redisdata.LogicData;
import service.FestivalService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Service
@Slf4j
public class FestivalServiceImpl extends ServiceImpl<FestivalMapper, Festival> implements FestivalService {

    @Override
    public FestivalVO readCache(Long id) {
        return null;
    }

    @Override
    public void updateCache(FestivalDTO festivalDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }


}
