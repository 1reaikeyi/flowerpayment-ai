package service;


import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.EmployeeDTO;
import model.dto.EmployeePageDTO;
import model.dto.LoginDTO;
import model.dto.PasswordDTO;
import model.entity.Employee;
import model.vo.EmployeeVO;

import java.util.List;

/**
 * 员工 Service（对应 employee 表）
 */

public interface EmployeeService extends IService<Employee> {

    Employee findEmployeename(String username);

    void register(EmployeeDTO employeeDTO);

    String login(LoginDTO loginDTO);

    void logout(Long userId);

    EmployeeVO readById(Long id);

    List<EmployeeVO> readPage(EmployeePageDTO employeePageDTO);

    void updateByObject(EmployeeDTO employeeDTO);

    void deleteById(List<Long> ids);

    void updatePassword(PasswordDTO passwordDTO, Long id);

    String admin1(LoginDTO loginDTO);

    void admin2(Long id);
}
