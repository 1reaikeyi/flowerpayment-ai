package start.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import start.oparation.OperationType;

import java.lang.reflect.Method;
import java.util.Arrays;
@Slf4j
@Aspect // 标记为AOP切面类
@Component
public class ServiceInterceptAspect {

    @Around("@annotation(start.aop.Logg)")
    public Object interceptServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息和目标方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 目标方法
        Method targetMethod = signature.getMethod();
        // 获取自定义注解
        Logg annotation = targetMethod.getAnnotation(Logg.class);
        // 注解的描述属性
        String methodDesc = annotation.desc();
        // 目标类名（比如com.rent.service.RentService）
        String className = joinPoint.getTarget().getClass().getName();
        // 目标方法名（比如queryRentInfo）
        String methodName = targetMethod.getName();
        // 方法入参
        Object[] methodArgs = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();
        Object result = null;
        log.info("=>执行：{}", methodDesc);
        try {
            // 3. 执行目标方法（核心业务逻辑）
            result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("=>目标类：{}, 目标方法：{}", className, methodName);
            log.info("耗时：{}ms | Rerurn：{}", costTime, result);
        } catch (Exception e) {
            // 5. 方法执行异常：打印异常信息
            long costTime = System.currentTimeMillis() - startTime;
            log.error("=>目标类：{}, 目标方法：{}", className, methodName);
            log.error("耗时：{}ms | 异常信息：{}", costTime, e.getMessage());
            throw e;
        }
        return result;
    }

    // 操作日志切面：拦截标注了 @OperationLogging 注解的方法，自动记录操作日志
    @Around("@annotation(start.aop.OperationLogging)")
    public Object interceptOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取目标方法上的 @OperationLogging 注解，取出操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        OperationLogging annotation = targetMethod.getAnnotation(OperationLogging.class);
        String operation = annotation.operation().name(); // 操作类型（CREATE/GET/UPDATE/DELETE）
        Object result = null;
        // 方法入参，转成字符串作为日志的 message
        // 注意：必须在 proceed() 之前计算，否则方法抛异常进入 catch 分支时 methodArgs 仍为 null，
        // 会导致 OperationType.error 内部 message.toString() 抛 NPE
        String methodArgs = Arrays.toString(joinPoint.getArgs());
        if (methodArgs == null || methodArgs.length() == 0) {
            methodArgs = "没有param,boby";
        }
        try {
            // 2. 执行目标业务方法，成功后记录操作日志（效果同 OperationType.ok）
            result = joinPoint.proceed();
            OperationType.ok(operation, methodArgs);
        } catch (Exception e) {
            // 3. 方法执行异常：记录错误操作日志（效果同 OperationType.error）
            OperationType.error(operation, methodArgs);
            throw e; // 异常继续向上抛，保证全局异常处理器能处理
        }
        return result;
    }
}
