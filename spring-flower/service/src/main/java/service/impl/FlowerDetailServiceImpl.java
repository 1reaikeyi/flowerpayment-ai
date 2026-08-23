package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.RedisPrefixConstant;
import lombok.extern.slf4j.Slf4j;
import mapper.FlowerDetailMapper;
import model.entity.Flower;
import model.entity.FlowerDetail;
import model.vo.FlowerVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redisdata.LogicData;
import service.FlowerDetailService;
import service.FlowerService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.*;

@Service
@Slf4j
public class FlowerDetailServiceImpl extends ServiceImpl<FlowerDetailMapper, FlowerDetail> implements FlowerDetailService {

}
