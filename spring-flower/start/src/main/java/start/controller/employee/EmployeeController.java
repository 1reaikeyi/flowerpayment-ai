package start.controller.employee;




import common.enums.OperationEnum;
import common.constant.ErrorConstant;
import start.aop.OperationLogging;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.EmployeeDTO;
import model.dto.LoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.EmployeeService;

import service.security.SecurityContextParam;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/register")
    public Result register(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.register(employeeDTO);
        return Result.success("register");
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        String token = employeeService.login(loginDTO);
        return Result.success(token);
    }

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping("/logout")
    public Result logout() {
        employeeService.logout();
        return Result.success("logout");
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result updateByObject(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateByObject(employeeDTO);
        return Result.success(employeeDTO.getId());
    }

}