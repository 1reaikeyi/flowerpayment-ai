package start.controller.admin;

import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.FlowerCategoryPageDTO;
import model.dto.FlowerCategoryDTO;
import model.vo.FestivalVO;
import model.vo.FlowerCategoryVO;
import model.vo.FlowerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.FlowerCategoryService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class AdminFlowerCategoryController {

    @Autowired
    private FlowerCategoryService flowerCategoryService;

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
    public Result readPage( @Validated FlowerCategoryPageDTO flowerCategoryPageDTO) {
        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategoryService.readPage(flowerCategoryPageDTO);
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

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/of/flower")
    public Result readFlower(@RequestParam("id") Long categoryId) {
        List<FlowerVO> flowerVOList = flowerCategoryService.readFlower(categoryId);
        return Result.success(flowerVOList);
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("of/festival")
    public Result getFestival(@RequestParam("id") Long categoryId) {
        List<FestivalVO> festivalList = flowerCategoryService.readFestival(categoryId);
        return Result.success(festivalList);
    }
}
