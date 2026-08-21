package excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.OnceAbsoluteMerge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@OnceAbsoluteMerge(firstRowIndex = 0, lastRowIndex = 0, firstColumnIndex = 3, lastColumnIndex = 4)
public class UserExcel {
    // 第1列：用户ID
    @ExcelProperty(index = 0, value = "用户ID")
    private Long id;

    // 第2列：姓名
    @ExcelProperty(index = 1, value = "姓名")
    private String name;

    // 第3列：电话
    @ExcelProperty(index = 2, value = "电话")
    private String phone;

    // 第4列：邮箱
    @ExcelProperty(index = 3, value = "邮箱")
    private String email;
}
