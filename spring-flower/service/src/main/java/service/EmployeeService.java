package service;


import com.baomidou.mybatisplus.extension.service.IService;
import common.result.PageResult;
import model.dto.EmployeeDTO;
import model.dto.EmployeePageDTO;
import model.dto.LoginDTO;
import model.dto.PasswordDTO;
import model.entity.Employee;
import model.vo.EmployeeVO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 员工 Service（对应 employee 表）
 */

public interface EmployeeService extends IService<Employee> {

    Employee findEmployeename(String username);

    String admin1(LoginDTO loginDTO);
    void admin2();

    void register(EmployeeDTO employeeDTO);
    String login(LoginDTO loginDTO);
    void logout();

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    EmployeeVO readById(Long id);
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    PageResult<EmployeeVO> readPage(EmployeePageDTO employeePageDTO);
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    void updateByObject(EmployeeDTO employeeDTO);
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    void deleteById(List<Long> ids);
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    void updatePassword(PasswordDTO passwordDTO);
}
