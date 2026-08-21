package service;


import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.EmployeeDTO;
import model.dto.EmployeePageDTO;
import model.dto.LoginDTO;
import model.entity.Employee;
import model.vo.EmployeeVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 员工 Service（对应 employee 表）
 */
@Service
public interface EmployeeService extends IService<Employee> {
    Employee findEmployeename(String username);

    void register(EmployeeDTO employeeDTO);

    String login(LoginDTO loginDTO);

    EmployeeVO readById(Long id);

    List<EmployeeVO> readPage(EmployeePageDTO employeePageDTO);

    void updateByObject(EmployeeDTO employeeDTO);

    void deleteById(List<Long> ids);

}
