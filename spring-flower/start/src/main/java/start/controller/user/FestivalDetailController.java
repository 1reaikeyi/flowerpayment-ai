package start.controller.user;

import common.enums.OperationEnum;
import common.result.Result;
import model.vo.FestivalDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.FestivalDetailService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/user/festivalDetail")
public class FestivalDetailController {

    @Autowired
    private FestivalDetailService festivalDetailService;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
//        return Result.success(flowerService.getById(id));
        FestivalDetailVO flowerDetailVO = festivalDetailService.readCache(id);
        return Result.success(flowerDetailVO);
    }

}
