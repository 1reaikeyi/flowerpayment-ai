//package start.controller.user;
//
//import common.enums.OperationEnum;
//import common.result.Result;
//import model.dto.CategoryPageDTO;
//import model.vo.FlowerCategoryVO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import service.FlowerCategoryService;
//import start.aop.OperationLogging;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/user/category")
//public class FlowerCategoryContrlller {
//
//    @Autowired
//    private FlowerCategoryService flowerCategoryService;
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping
//    public Result readByType(@RequestParam("type") Long type) {
//        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategoryService.readByType(type);
//        return Result.success(flowerCategoryVOList);
//    }
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/all")
//    public Result readPage( @Validated CategoryPageDTO categoryPageDTO) {
//        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategoryService.readPage(categoryPageDTO);
//        return Result.success(flowerCategoryVOList);
//    }
//}
