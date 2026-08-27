package start.controller.admin;

import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.FlowerDTO;
import model.dto.FlowerDetailDTO;
import model.dto.FlowerPageDTO;
import model.vo.FlowerDetailVO;
import model.vo.FlowerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.FlowerDetailService;
import service.FlowerService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/flower")
@Slf4j
public class AdminFlowerDetailController {
    @Autowired
    private FlowerDetailService flowerDetailService;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result add(@RequestBody FlowerDetailDTO flowerDetailDTO) {
        FlowerDetailDTO dto = flowerDetailService.create(flowerDetailDTO);
        return Result.success(dto);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
//        return Result.success(flowerService.getById(id));
        FlowerDetailVO flowerDetailVO = flowerDetailService.readCache(id);
        return Result.success(flowerDetailVO);
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateByObject(@RequestBody FlowerDetailDTO flowerDetailDTO) {
        flowerDetailService.updateCache(flowerDetailDTO);
        return Result.success(flowerDetailDTO);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteById(@RequestParam List<Long> ids) {
        flowerDetailService.deleteCache(ids);
        return Result.success(ids);
    }

}
