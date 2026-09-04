//package start.controller.user;
//
//import common.enums.OperationEnum;
//import common.result.Result;
//import model.dto.UserAddressDTO;
//import model.entity.UserAddress;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import service.UserAddressService;
//import start.aop.OperationLogging;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/user/address")
//public class UserAddressController {
//    @Autowired
//    private UserAddressService userAddressService;
//
//    private static final Long LIMIT = 5L;
//    @OperationLogging(operation = OperationEnum.CREATE)
//    @PostMapping
//    public Result createAddress(UserAddressDTO userAddressDTO) {
////        UserAddress userAddress = BeanUtil.toBean(userAddressDTO, UserAddress.class);
////        Long userId = SecurityContextParam.getCurrentUserId();
////        if(userAddress.getIsDefault() == 1){
////            UserAddress defaultAddress = userAddressService.lambdaQuery()
////                    .eq(UserAddress::getUserId,userId)
////                    .eq(UserAddress::getIsDefault,1)
////                    .one();
////            defaultAddress.setIsDefault(0L);
////            userAddressService.updateById(defaultAddress);
////        }
////        userAddressService.save(userAddress);
//        UserAddressDTO dto = userAddressService.create(userAddressDTO);
//        return Result.success(dto);
//    }
//
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("default")
//    public Result readDefaultAddress() {
////        Long userId = SecurityContextParam.getCurrentUserId();
////        UserAddress defaultAddress = userAddressService.lambdaQuery()
////                .eq(UserAddress::getUserId,userId)
////                .eq(UserAddress::getIsDefault,1).one();
//        UserAddress defaultAddress = userAddressService.readDefaultAddress();
//        return Result.success(defaultAddress);
//    }
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/all")
//    public Result readAddress(Long offset, Long current) {
////        ScrollResult scrollResult = new ScrollResult();
////        offset = offset == null ? LIMIT : offset;
////        Long userId = SecurityContextParam.getCurrentUserId();
////        List<UserAddress> userAddressList = userAddressService.lambdaQuery()
////                .eq(UserAddress::getUserId,userId)
////                .last("limit "+offset)
////                .list();
////        if(userAddressList.size() == 0 || userAddressList == null){
////            scrollResult.setList(null);
////            scrollResult.setMinTime(0L);
////            scrollResult.setOffset(LIMIT);
////            return Result.success(scrollResult);
////        }
////        scrollResult.setList(userAddressList);
////        scrollResult.setMinTime(userAddressList.get(userAddressList.size() - 1).getId());
////        scrollResult.setOffset(LIMIT);
////        ScrollResult<UserAddress> scrollResult = userAddressService.readPage();
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping("/default/{id}")
//    public Result updateDefaultAddress(@PathVariable Long id) {
////        Long userId = SecurityContextParam.getCurrentUserId();
////        UserAddress defaultAddress = userAddressService.lambdaQuery()
////                .eq(UserAddress::getUserId,userId)
////                .eq(UserAddress::getIsDefault,1)
////                .one();
////        defaultAddress.setIsDefault(0L);
////        userAddressService.updateById(defaultAddress);
////        UserAddress userAddress  = userAddressService.lambdaQuery()
////                .eq(UserAddress::getUserId,userId)
////                .eq(UserAddress::getId, id).one();
////        userAddress.setIsDefault(1L);
////        userAddressService.updateById(userAddress);
//        userAddressService.updateDefaultAddress(id);
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.UPDATE)
//    @PutMapping
//    public Result updateAddress(UserAddressDTO userAddressDTO) {
////        Long userId = SecurityContextParam.getCurrentUserId();
////        UserAddress userAddress = BeanUtil.toBean(userAddressDTO, UserAddress.class);
////        if(userAddress.getIsDefault() == 1){
////            UserAddress defaultAddress = userAddressService.lambdaQuery()
////                    .eq(UserAddress::getUserId,userId)
////                    .eq(UserAddress::getIsDefault,1).one();
////            defaultAddress.setIsDefault(0L);
////            userAddressService.updateById(defaultAddress);
////        }
//        userAddressService.updateAddress(userAddressDTO);
//        return Result.success();
//    }
//    @OperationLogging(operation = OperationEnum.DELETE)
//    @DeleteMapping
//    public Result deleteAddress(List<Long> ids) {
//        userAddressService.aeleteAddress(ids);
//        return Result.success();
//    }
//
//}
