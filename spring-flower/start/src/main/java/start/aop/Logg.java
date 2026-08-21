package start.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 注解作用目标：METHOD（只能标注在方法上）
@Target({ElementType.METHOD})
// 注解保留策略：RUNTIME（运行时生效，AOP才能识别）
@Retention(RetentionPolicy.RUNTIME)
// 可选：@Documented（生成文档时包含该注解）
public @interface Logg {
    /**
     * 自定义属性：方法描述（可选，用于日志展示）
     */
    String desc() default "未描述";

    /**
     * 自定义属性：是否记录耗时（默认记录）
     */
    boolean recordCostTime() default true;
}