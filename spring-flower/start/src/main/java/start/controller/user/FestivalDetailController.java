//package start.controller.user;
//
//import common.enums.OperationEnum;
//import common.result.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import service.FestivalDetailService;
//import start.aop.OperationLogging;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/user/festivalDetail")
//public class FestivalDetailController {
//
//    @Autowired
//    private FestivalDetailService festivalDetailService;
//
//    @OperationLogging(operation = OperationEnum.DELETE)
//    @DeleteMapping
//    public Result deleteById(@RequestParam List<Long> ids) {
//        festivalDetailService.deleteCache(ids);
//        return Result.success(ids);
//    }
//}
