//package start.controller.user;
//
//import common.enums.OperationEnum;
//import common.result.Result;
//import model.dto.FlowerPageDTO;
//import model.vo.FlowerDetailVO;
//import model.vo.FlowerVO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import service.FlowerDetailService;
//import service.FlowerService;
//import start.aop.OperationLogging;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/user/flower")
//public class FlowerController {
//
//    @Autowired
//    private FlowerService flowerService;
//    @Autowired
//    private FlowerDetailService flowerDetailService;
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping
//    public Result readById(@RequestParam Long id) {
////        return Result.success(flowerService.getById(id));
//        FlowerVO flowerVO = flowerService.readCache(id);
//        return Result.success(flowerVO);
//    }
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/all")
//    public Result readPage(FlowerPageDTO flowerPageDTO) {
//        List<FlowerVO> flowerVOList = flowerService.readPage(flowerPageDTO);
//        return Result.success(flowerVOList);
//    }
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/of/flowerDetail")
//    public Result readFlowerDetail(@RequestParam Long id) {
//        List<FlowerDetailVO> flowerDetailVOList = flowerService.readFestivalDetail(id);
//        return Result.success(flowerDetailVOList);
//    }
//}
