package start.controller.admin;




import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.*;
import common.enums.OperationEnum;
import common.constant.ErrorConstant;
import common.exception.LoginFailedException;
import model.dto.EmployeePageDTO;
import model.vo.EmployeeVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import start.aop.OperationLogging;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import model.dto.EmployeeDTO;
import model.dto.LoginDTO;
import model.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import service.EmployeeService;

import start.security.SecurityContextParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


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
        // 获取当前登录用户ID
        Long userId = SecurityContextParam.getCurrentUserId();
        if (userId == null) {
            return Result.error(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        stringRedisTemplate.delete(RoleConstant.ROLE_ADMIN+ userId);
        SecurityContextHolder.clearContext();
        return Result.success("logout");
    }
    @PreAuthorize("hasAuthority(T(common.constant.RoleConstant).ROLE_ADMIN)")
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
    @PutMapping
    public Result updateByObject(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateByObject(employeeDTO);
        return Result.success(employeeDTO.getId());
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result deleteById(@RequestParam List<Long> ids) {
        employeeService.deleteById(ids);
        return Result.success(ids);
    }



}