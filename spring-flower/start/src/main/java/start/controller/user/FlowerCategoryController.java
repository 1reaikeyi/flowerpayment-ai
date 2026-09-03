package start.controller.user;

import common.enums.OperationEnum;
import common.result.Result;
import model.dto.FlowerCategoryPageDTO;
import model.vo.FestivalVO;
import model.vo.FlowerCategoryVO;
import model.vo.FlowerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.FlowerCategoryService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/user/category")
public class FlowerCategoryController {

    @Autowired
    private FlowerCategoryService flowerCategoryService;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readByType(@RequestParam("type") Long type) {
        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategoryService.readByType(type);
        return Result.success(flowerCategoryVOList);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage( @Validated FlowerCategoryPageDTO flowerCategoryPageDTO) {
        return Result.success(flowerCategoryService.readPage(flowerCategoryPageDTO));
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
