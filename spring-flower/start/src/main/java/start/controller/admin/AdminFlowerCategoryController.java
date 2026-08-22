package start.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.alipay.api.domain.CategoryDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.RedisPrefixConstant;
import common.constant.StatusConstant;
import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.CategoryPageDTO;
import model.dto.FlowerCategoryDTO;
import model.entity.Festival;
import model.entity.Flower;
import model.entity.FlowerCategory;
import model.vo.FlowerCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.FestivalService;
import service.FlowerCategoryService;
import service.FlowerService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class AdminFlowerCategoryController {

    @Autowired
    private FlowerCategoryService flowerCategoryService;
    @Autowired
    private FlowerService flowerService;
    @Autowired
    private FestivalService festivalService;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result create(@RequestBody FlowerCategoryDTO flowerCategoryDTO) {
        FlowerCategoryDTO saved = flowerCategoryService.create(flowerCategoryDTO);
        return Result.success(saved);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readByType(@RequestParam("type") Long type) {
        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategoryService.readByType(type);
        return Result.success(flowerCategoryVOList);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage( @Validated CategoryPageDTO categoryPageDTO) {
        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategoryService.readPage(categoryPageDTO);
        return Result.success(flowerCategoryVOList);
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateByObject(@RequestBody FlowerCategoryDTO categoryDTO) {
        flowerCategoryService.updateByObject(categoryDTO);
        return Result.success(categoryDTO);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteById(@RequestParam List<Long> ids) {
        flowerCategoryService.deleteById(ids);
        return Result.success(ids);
    }

//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/of/flower")
//    public Result readFlower(@RequestParam("id") Long categoryId) {
//        List<FlowerVO> flowerVOList = flowerCategoryService.readFlower(categoryId);
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("of/festival")
//    public Result getFestival(@RequestParam("id") Long categoryId) {
//        List<FestivalVO> festivalList = festivalService.readFestival(categoryId);
//        return Result.success();
//    }
}
