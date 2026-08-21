package service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.*;
import common.enums.OperationErrorEnum;
import common.exception.EmployeeFailedException;
import common.exception.LoginFailedException;
import common.properties.JwtProperties;
import common.utils.JwtUtil;
import mapper.EmployeeMapper;
import model.dto.EmployeeDTO;
import model.dto.EmployeePageDTO;
import model.dto.LoginDTO;
import model.vo.EmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import model.entity.Employee;
import org.springframework.transaction.annotation.Transactional;
import service.EmployeeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 员工 Service（对应 employee 表）
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码加密器 Bean
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;
    // 懒加载：打破 认证管理器 -> 认证提供者 -> 员工Service 的循环依赖
    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager; // 注入认证管理器

    @Override
    public Employee findEmployeename(String username) {
        return super.lambdaQuery().eq(Employee::getUsername, username).one();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(EmployeeDTO employeeDTO) {
        Employee find = this.findEmployeename(employeeDTO.getUsername());
        if (find != null) {
            throw new LoginFailedException(ErrorConstant.REGISTER_ERROR);
        }
        Employee employee = BeanUtil.toBean(employeeDTO, Employee.class);
        employee.setStatus(StatusConstant.ENABLE);
        if (employee.getPassword() == null) {
            employee.setPassword("123456");
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setCreateUser(0L);
        employee.setUpdateUser(0L);
        super.save(employee);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                username, password,
                Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN)));
        authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        // 认证成功后，查询用户完整信息
        Employee employee = this.findEmployeename(username);
        Map<String,Object> map = new HashMap<>();
        map.put(JwtConstant.EMP_ID, employee.getId());
        map.put(JwtConstant.EMP_NAME, employee.getUsername());
        map.put(JwtConstant.TYPE,RoleConstant.ROLE_ADMIN);

        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), map);
        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.EMP_AUTH_PREFIX+ employee.getId(), token,
                jwtProperties.getAdminTtl(), TimeUnit.SECONDS);
        return token;
    }

    @Override
    public EmployeeVO readById(Long id) {
        Employee employee = super.getById(id);
        if (employee == null) {
            throw new EmployeeFailedException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        EmployeeVO employeeVO = BeanUtil.toBean(employee, EmployeeVO.class);
        return employeeVO;
    }

    @Override
    public List<EmployeeVO> readPage(EmployeePageDTO employeePageDTO) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(employeePageDTO.getEmployeename()!= null,
                Employee::getUsername,employeePageDTO.getEmployeename());
        IPage page = new Page(employeePageDTO.getPage(),employeePageDTO.getPageSize());
        IPage<Employee> employeeIPage = super.page(page,queryWrapper);
        List<EmployeeVO> voList = employeeIPage.getRecords().stream()
                .map(emp -> {
                    EmployeeVO vo = BeanUtil.copyProperties(emp, EmployeeVO.class);
                    return vo;
                })
                .collect(Collectors.toList());
        return voList;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByObject(EmployeeDTO employeeDTO) {
        // 1. 校验 ID
        if (employeeDTO.getId() == null) {
            throw new EmployeeFailedException("员工ID不能为空");
        }

        // 2. 构建 wrapper，只 set 有值的字段
        LambdaUpdateWrapper<Employee> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Employee::getId, employeeDTO.getId());

        if (StrUtil.isNotBlank(employeeDTO.getUsername())) {
            updateWrapper.set(Employee::getUsername, employeeDTO.getUsername());
        }
        if (StrUtil.isNotBlank(employeeDTO.getPhone())) {
            updateWrapper.set(Employee::getPhone, employeeDTO.getPhone());
        }
        if (StrUtil.isNotBlank(employeeDTO.getEmail())) {
            updateWrapper.set(Employee::getEmail, employeeDTO.getEmail());
        }
        if (StrUtil.isNotBlank(employeeDTO.getAvatar())) {
            updateWrapper.set(Employee::getAvatar, employeeDTO.getAvatar());
        }
        if (StrUtil.isNotBlank(employeeDTO.getSex())) {
            updateWrapper.set(Employee::getSex, employeeDTO.getSex());
        }
        if (employeeDTO.getStatus() != null) {
            updateWrapper.set(Employee::getStatus, employeeDTO.getStatus());
        }
        // 密码单独处理（加密）
        if (StrUtil.isNotBlank(employeeDTO.getPassword())) {
            updateWrapper.set(Employee::getPassword,
                    passwordEncoder.encode(employeeDTO.getPassword()));
        }

        // 3. 执行更新（entity 传 null，完全用 wrapper 里的 set）
        try {
            super.update(null, updateWrapper);
        } catch (Exception e) {
            throw new EmployeeFailedException(OperationErrorEnum.UPDATE_ERROR.name());
        }

    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(List<Long> ids) {
        try {
            super.removeByIds(ids);
        } catch (Exception e) {
            throw new EmployeeFailedException(OperationErrorEnum.DELETE_ERROR.name());
        }

    }


}
