//package start.controller.user;
//
//import common.constant.ErrorConstant;
//import common.enums.OperationEnum;
//import common.result.Result;
//import model.dto.EmployeeDTO;
//import model.dto.LoginDTO;
//import model.dto.UserDTO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import service.EmployeeService;
//import service.UserService;
//import start.aop.OperationLogging;
//import start.security.SecurityContextParam;
//
//@RestController
//@RequestMapping("/user")
//public class UserController {
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/register")
//    public Result register(@RequestBody UserDTO userDTO) {
//
//        return Result.success("register");
//    }
//
//    @PostMapping("/login")
//    public Result login(@RequestBody LoginDTO loginDTO) {
//
//        return Result.success();
//    }
//
//    @OperationLogging(operation = OperationEnum.CREATE)
//    @PostMapping("/logout")
//    public Result logout() {
//        // 获取当前登录用户ID
//        Long userId = SecurityContextParam.getCurrentUserId();
//        if (userId == null) {
//            return Result.error(ErrorConstant.ACCOUNT_NOT_EXIST);
//        }
//
//        return Result.success("logout");
//    }
//}
