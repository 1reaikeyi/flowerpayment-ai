package start.controller.admin;

import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.FlowerDTO;
import model.dto.FlowerPageDTO;
import model.vo.FlowerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import service.FlowerDetailService;
import service.FlowerService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/flower")
@Slf4j
public class AdminFlowerController {

    @Autowired
    private FlowerService flowerService;
    @Autowired
    private FlowerDetailService flowerDetailService;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result create(@RequestBody FlowerDTO flowerDTO) {
        FlowerDTO dto = flowerService.create(flowerDTO);
        return Result.success(dto);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
        FlowerVO flowerVO = flowerService.readCache(id);
        return Result.success(flowerVO);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage(FlowerPageDTO flowerPageDTO) {
        List<FlowerVO> flowerVOList = flowerService.readPage(flowerPageDTO);
        return Result.success(flowerVOList);
    }



    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateByObject(@RequestBody FlowerDTO flowerDTO) {
        flowerService.updateCache(flowerDTO);
        return Result.success(flowerDTO);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteById(@RequestParam List<Long> ids) {
        flowerService.deleteCache(ids);
        return Result.success(ids);
    }

}
