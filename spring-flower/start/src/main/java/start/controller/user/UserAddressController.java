package start.controller.user;

import common.enums.OperationEnum;
import common.result.Result;
import common.result.ScrollResult;
import model.dto.UserAddressDTO;
import model.entity.UserAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.UserAddressService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/user/address")
public class UserAddressController {
    @Autowired
    private UserAddressService userAddressService;


    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result createAddress(UserAddressDTO userAddressDTO) {
        UserAddressDTO dto = userAddressService.create(userAddressDTO);
        return Result.success(dto);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("default")
    public Result readDefaultAddress() {

        UserAddress defaultAddress = userAddressService.readDefaultAddress();
        return Result.success(defaultAddress);
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAddress(Long offset, Long current) {
        ScrollResult scrollResult = userAddressService.readPage(offset,current);
        return Result.success(scrollResult);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("/default/{id}")
    public Result updateDefaultAddress(@PathVariable Long id) {

        userAddressService.updateDefaultAddress(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateAddress(UserAddressDTO userAddressDTO) {

        userAddressService.updateAddress(userAddressDTO);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteAddress(List<Long> ids) {
        userAddressService.deleteAddress(ids);
        return Result.success();
    }

}
