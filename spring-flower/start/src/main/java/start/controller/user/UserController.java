package start.controller.user;

import common.constant.ErrorConstant;
import common.enums.OperationEnum;
import common.result.Result;
import model.dto.LoginDTO;
import model.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.UserService;
import start.aop.OperationLogging;
import service.security.SecurityContextParam;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return Result.success("register");
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success();
    }

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping("/logout")
    public Result logout() {
        userService.logout();
        return Result.success("logout");
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateByObject(@RequestBody UserDTO userDTO) {
        userService.updateByObject(userDTO);
        return Result.success(userDTO.getId());
    }
}
