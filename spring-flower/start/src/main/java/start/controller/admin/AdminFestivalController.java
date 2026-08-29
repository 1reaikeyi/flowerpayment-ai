package start.controller.admin;

import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.FestivalDTO;
import model.dto.FestivalPageDTO;
import model.dto.FlowerDTO;
import model.dto.FlowerPageDTO;
import model.vo.FestivalDetailVO;
import model.vo.FestivalVO;
import model.vo.FlowerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.FestivalDetailService;
import service.FestivalService;
import service.FlowerService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/festival")
@Slf4j
public class AdminFestivalController {

    @Autowired
    private FestivalService festivalService;


    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result create(@RequestBody FestivalDTO festivalDTO) {

        FestivalDTO dto = festivalService.create(festivalDTO);
        return Result.success(dto);
    }

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
        List<FestivalVO> festivalVOList = festivalService.readPage(festivalPageDTO);
        return Result.success(festivalVOList);
    }



    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateByObject(@RequestBody FestivalDTO festivalDTO) {
        festivalService.updateCache(festivalDTO);
        return Result.success(festivalDTO);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteById(@RequestParam List<Long> ids) {
        festivalService.deleteCache(ids);
        return Result.success(ids);
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
