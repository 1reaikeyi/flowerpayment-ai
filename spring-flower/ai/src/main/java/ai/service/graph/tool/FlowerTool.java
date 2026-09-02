package ai.service.graph.tool;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import model.entity.Flower;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.FlowerService;

import java.util.List;
import java.util.Optional;

@Component
public class FlowerTool {
    @Autowired
    private FlowerService flowerService;

    private static final String FIELD_NAME_RESULT = "{}_{}";  // 提取格式字符串常量
    private static final String READ_BY_ID = "根据id";
    @Tool(description = READ_BY_ID)
    public FlowerJson queryById(@ToolParam(description = READ_BY_ID) Long flowerId,
                                      ToolContext toolContext) {
        return Optional.ofNullable(flowerId)
                .map(id -> flowerService.getById(id))
                .map(flower -> FlowerJson.of(flower))
                .map(flowerJson -> {
                    // 存储数据的字段名：使用Java原生String.format
                    String className = FlowerJson.class.getSimpleName();
                    // 将首字母转为小写
                    String lowerClassName = className.isEmpty() ? className
                            : Character.toLowerCase(className.charAt(0)) + className.substring(1);
                    String field = String.format(FIELD_NAME_RESULT, lowerClassName, flowerJson.getId());
                    // 存储的key
                    Object requestIdObj = toolContext.getContext().get(READ_BY_ID);
                    String requestId = requestIdObj != null ? String.valueOf(requestIdObj) : null;
                    ToolResultHolder.put(requestId, field, flowerJson);
                    return flowerJson;
                })
                .orElse(null);
    }
    @Tool(description = "根据多个条件组合查询鲜花列表，支持按名称、颜色、分类、价格区间、状态等筛选")
    public List<FlowerJson> queryFlowers(
            @ToolParam(description = "鲜花多条件查询参数，所有字段均为可选") FlowerToolParam param) {

        LambdaQueryWrapper<Flower> wrapper = new LambdaQueryWrapper<>();

        // 名称模糊查
        if (StrUtil.isNotBlank(param.getName())) {
            wrapper.like(Flower::getName, param.getName());
        }
        // 分类精确查
        if (param.getCategoryId() != null) {
            wrapper.eq(Flower::getCategoryId, param.getCategoryId());
        }
        // 颜色精确查
        if (StrUtil.isNotBlank(param.getColor())) {
            wrapper.eq(Flower::getColor, param.getColor());
        }
        // 价格区间
        if (param.getPrice() != null) {
            wrapper.ge(Flower::getPrice, param.getPrice());
        }
        if (param.getPrice() != null) {
            wrapper.le(Flower::getPrice, param.getPrice());
        }
        // 状态
        if (param.getStatus() != null) {
            wrapper.eq(Flower::getStatus, param.getStatus());
        }
        // 描述模糊查
        if (StrUtil.isNotBlank(param.getDescription())) {
            wrapper.like(Flower::getDescription, param.getDescription());
        }

        // 默认按创建时间倒序，最新的在前
        wrapper.orderByDesc(Flower::getCreateTime);

        // 执行查询 + 转 DTO
        return flowerService.list(wrapper).stream()
                .map(flower -> BeanUtil.toBean(flower,FlowerJson.class))
                .toList();
    }
}