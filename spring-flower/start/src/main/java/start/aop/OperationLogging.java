package start.aop;

import common.enums.OperationEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在方法上，通过 AOP 自动记录操作日志
 */
// 注解作用目标：METHOD（只能标注在方法上）
@Target({ElementType.METHOD})
// 注解保留策略：RUNTIME
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLogging {
    /**
     * 操作类型（对应 OperationEnum：CREATE/GET/UPDATE/DELETE）
     */
    OperationEnum operation() default OperationEnum.CREATE;
}
