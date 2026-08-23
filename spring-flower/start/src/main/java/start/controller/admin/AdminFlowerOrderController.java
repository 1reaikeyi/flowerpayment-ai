//package start.controller.admin;
//
//import com.alipay.api.response.AlipayTradeRefundResponse;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import common.enums.OperationEnum;
//import common.result.Result;
//import lombok.extern.slf4j.Slf4j;
//
//import model.dto.FlowerOrderPageDTO;
//import model.entity.FlowerOrder;
//import model.entity.FlowerOrderDetail;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import service.FlowerOrderDetailService;
//import service.FlowerOrderPayService;
//import service.FlowerOrderService;
//import service.FlowerService;
//import start.aop.OperationLogging;
//import start.controller.zhifubao.DTO.RefundDTO;
//import start.controller.zhifubao.service.ZhifubaoService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/admin/flowerOrder")
//@Slf4j
//public class AdminFlowerOrderController {
//    @Autowired
//    private FlowerOrderService flowerOrderService;
//    @Autowired
//    private FlowerOrderDetailService flowerOrderDetailService;
//    @Autowired
//    private FlowerOrderPayService flowerOrderPayService;
//    @Autowired
//    private ZhifubaoService zhifubaoService;
//
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping
//    public Result readByFlowerOrderId(@RequestParam("id") Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        List<FlowerOrderDetail> flowerOrderDetails = flowerOrderDetailService.lambdaQuery()
//                .in(FlowerOrderDetail::getFlowerOrderId, id).list();
//
//        return Result.success(flowerOrderDetails);
//    }
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/all")
//    public Result readAll(FlowerOrderPageDTO flowerOrderPageDTO) {
//        LambdaQueryWrapper<FlowerOrder> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(flowerOrderPageDTO.getStatus()!= null,FlowerOrder::getStatus,flowerOrderPageDTO.getStatus());
//        IPage<FlowerOrder> page = new Page<>(flowerOrderPageDTO.getPage(), flowerOrderPageDTO.getPageSize());
//        IPage<FlowerOrder> flowerOrderIPage = flowerOrderService.page(page, queryWrapper);
//        return Result.success(flowerOrderIPage);
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("cooking/{id}")
//    public Result update3(@PathVariable Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        flowerOrder.setStatus(FlowerOrderStatusEnum.COOKING);
//        flowerOrderService.updateById(flowerOrder);
//        return Result.success(flowerOrder.getStatus());
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("go/{id}")
//    public Result update4(@PathVariable Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        flowerOrder.setStatus(FlowerOrderStatusEnum.GO);
//        flowerOrderService.updateById(flowerOrder);
//        return Result.success(flowerOrder.getStatus());
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("delivering/{id}")
//    public Result update5(@PathVariable Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        flowerOrder.setStatus(FlowerOrderStatusEnum.DELIVERING);
//        flowerOrderService.updateById(flowerOrder);
//        return Result.success(flowerOrder.getId());
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("arrived/{id}")
//    public Result update6(@PathVariable Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        flowerOrder.setStatus(FlowerOrderStatusEnum.ARRIVED);
//        flowerOrderService.updateById(flowerOrder);
//        return Result.success(flowerOrder.getStatus());
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("/complete/{id}")
//    public Result update7(@PathVariable Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        flowerOrder.setStatus(FlowerOrderStatusEnum.COMPLETED);
//        flowerOrderService.updateById(flowerOrder);
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("canceled/{id}")
//    public Result update8(@PathVariable Long id) {
//        FlowerOrder flowerOrder = flowerOrderService.lambdaQuery().eq(FlowerOrder::getId, id).one();
//        FlowerOrderPay flowerOrderPay = flowerOrderPayService.lambdaQuery().eq(FlowerOrderPay::getFlowerOrderId, id).one();
//        flowerOrder.setStatus(FlowerOrderStatusEnum.CANCELLED);
//
//        RefundDTO refundDTO = new RefundDTO();
//        refundDTO.setRefundAmount(flowerOrderPay.getAmount());
//        refundDTO.setOutTradeNo(flowerOrderPay.getId().toString());
//        refundDTO.setOutRefundNo(flowerOrderPay.getId().toString());
//        refundDTO.setRefundReason("XXXXXXXXXXXXXXXXXXXXXXX");
//
//
//        try {
//            refund(refundDTO);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        flowerOrderPayService.updateById(flowerOrderPay);
//        return Result.success(flowerOrder.getStatus());
//    }
//    public AlipayTradeRefundResponse refund(RefundDTO refundDTO) throws Exception {
//        return zhifubaoService.refund(refundDTO);
//    }
//}
