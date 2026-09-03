package model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeVO implements Serializable {
    private Long id;
    private String username;
    private String avatar;
    private String work;
    private String sex;      // 性别：男 / 女
    private String email;
    private String phone;
    private Long status;
}
