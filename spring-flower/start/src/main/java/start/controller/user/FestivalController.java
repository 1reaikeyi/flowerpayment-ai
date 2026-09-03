package start.controller.user;

import common.enums.OperationEnum;
import common.result.Result;
import model.dto.FestivalPageDTO;
import model.vo.FestivalDetailVO;
import model.vo.FestivalVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.FestivalService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/user/festival")
public class FestivalController {

    @Autowired
    private FestivalService festivalService;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
//        return Result.success(festivalService.getById(id));
        FestivalVO festivalVO = festivalService.readCache(id);
        return Result.success(festivalVO);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage(FestivalPageDTO festivalPageDTO) {
        return Result.success(festivalService.readPage(festivalPageDTO));
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/of/festivalDetail")
    public Result readFestivalDetail(@RequestParam Long id) {
        List<FestivalDetailVO> festivalDetailVOList = festivalService.readFestivalDetail(id);
        return Result.success(festivalDetailVOList);
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/of/flower")
    public Result readFlower(@RequestParam Long id) {
        List<FestivalDetailVO> festivalDetailVOList = festivalService.readFlower(id);
        return Result.success(festivalDetailVOList);
    }
}

