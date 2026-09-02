package start.controller.admin;

import common.constant.ErrorConstant;
import common.constant.RoleConstant;
import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.EmployeeDTO;
import model.dto.EmployeePageDTO;
import model.dto.LoginDTO;
import model.dto.PasswordDTO;
import model.entity.Employee;
import model.vo.EmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.EmployeeService;
import start.aop.OperationLogging;
import start.security.SecurityContextParam;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        String one = employeeService.admin1(loginDTO);
        return Result.success(one);
    }
    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping("/logout")
    public Result logout() {
        // 获取当前登录用户ID
        Long userId = SecurityContextParam.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        employeeService.admin2(userId);
        return Result.success("logout");
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
        EmployeeVO employeeVO = employeeService.readById(id);
        return Result.success(employeeVO);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage( @Validated EmployeePageDTO employeePageDTO) {
        List<EmployeeVO> employeeVOList = employeeService.readPage(employeePageDTO);
        return Result.success(employeeVOList);
    }


    @OperationLogging(operation = OperationEnum.UPDATE)
    @DeleteMapping("/password")
    public Result updatePassword(@Validated @RequestBody PasswordDTO passwordDTO) {
        Long id = SecurityContextParam.getCurrentUserId();
        employeeService.updatePassword(passwordDTO,id);
        return Result.success(id);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteById(@RequestParam List<Long> ids) {
        employeeService.deleteById(ids);
        return Result.success(ids);
    }
}
