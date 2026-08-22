package start.oparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import start.security.SecurityContextParam;

/**
 * 数据库操作类型OperationType
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class OperationType {
    private String operation;
    private Long id;
    private String type;
    private String status;
    private Object message;

    public static OperationType ok(String operation,Object message) {
        OperationType operationType = new OperationType();
        operationType.operation = operation;
        operationType.id = SecurityContextParam.getCurrentUserId();
        operationType.type = SecurityContextParam.getCurrentType();
        operationType.status = "SUCCESS";
        operationType.message = message;
        if(message.toString().equals("password")){
            message = "不许偷看";
        }
        log.info("role: " + operationType.type+" , :ID:"+operationType.id+", 执行操作: "+operationType.operation
                +", 使用参数: "+ message +", "+operationType.status);
        return operationType;
    }

    public static OperationType error(String operation,Object message) {
        OperationType operationType = new OperationType();
        operationType.operation = operation;
        operationType.id = SecurityContextParam.getCurrentUserId();
        operationType.type = SecurityContextParam.getCurrentType();
        operationType.status = "ERROR";
        if(message.toString().equals("password")){
            message = "不许偷看";
        }
        log.info("role: " + operationType.type+", :ID:"+operationType.id+", 执行操作:"+operationType.operation
                +", 使用参数: "+ message +", "+operationType.status);
        return operationType;
    }
}
