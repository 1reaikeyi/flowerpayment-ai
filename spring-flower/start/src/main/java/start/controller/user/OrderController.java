//package start.controller.user;
//
//
//import common.enums.OperationEnum;
//import common.result.Result;
//import jakarta.servlet.http.HttpServletResponse;
//import model.dto.FlowerOrderPageDTO;
//import model.dto.FlowerPageDTO;
//import model.vo.FlowerOrderVO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import service.FlowerOrderService;
//import start.aop.OperationLogging;
//import start.controller.zhifubao.DTO.PayDTO;
//import start.controller.zhifubao.DTO.RefundDTO;
//import start.controller.zhifubao.service.ZhifubaoService;
//
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@RestController
//@RequestMapping("/user/order")
//public class OrderController {
//    @Autowired
//    private FlowerOrderService flowerOrderService;
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping
//    public Result readByOrderId(@RequestParam("id") Long id) {
//        FlowerOrderVO flowerOrderVO = flowerOrderService.readById(id);
//        return Result.success(flowerOrderVO);
//    }
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/history")
//    public Result readPage(FlowerOrderPageDTO flowerOrderPageDTO) {
//        List<FlowerOrderVO> flowerOrderVOList = flowerOrderService.readPage(flowerOrderPageDTO);
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PostMapping("order/{id}")
//    public Result update1(@PathVariable Long id) {
//        flowerOrderService.update1();
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("payment/{id}")
//    public Result update2(@PathVariable Long id, HttpServletResponse response) {
//        flowerOrderService.update2();
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("/complete/{id}")
//    public Result update7(@PathVariable Long id) {
//        flowerOrderService.update7(id);
//        return Result.success();
//    }
//
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("canceled/{id}")
//    public Result update8(@PathVariable Long id) {
//        flowerOrderService.update8(id);
//        return Result.success();
//    }
//}
