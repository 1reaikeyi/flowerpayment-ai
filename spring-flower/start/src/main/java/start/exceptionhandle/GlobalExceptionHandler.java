package start.exceptionhandle;

import common.constant.ErrorConstant;
import common.exception.BaseException;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常 BaseException
     * 返回 200 状态码 + Result.error
     */
    @ExceptionHandler(BaseException.class)
    public Result exception(BaseException e) {
        return Result.error(e.getMessage() + ">>>>去联系管理员");
    }

    /**
     * 防小人，不防君子
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);  // 关键:打印堆栈,方便排查
        return Result.error("服务器开小差了,请稍后再试");
    }
    /**
     * 处理数据库唯一约束冲突（如重复用户名）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSQLIntegrityConstraintViolationException(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split("'");
            String username = split[1];
            String Message = username + ErrorConstant.USERNAME_EXIST;
            return Result.error(Message);
        } else {
            return Result.error(ErrorConstant.ERROR + e.getMessage());
        }
    }
    /**
     *  ① 数据库层(JDBC):
     *      MySQL 抛 java.sql.SQLIntegrityConstraintViolationException   (checked SQLException)
     *
     *   ② MyBatis 内核(mapper 执行器):
     *      └─ ExceptionFactory.wrapException() 把 SQLException 包成
     *          org.apache.ibatis.exceptions.PersistenceException        (RuntimeException)
     *
     *   ③ mybatis-spring(SqlSessionTemplate 代理):
     *      └─ MybatisExceptionTranslator.translateExceptionIfPossible()
     *           ├─ 解包出 cause = SQLException
     *           └─ 交给 Spring 的 SQLErrorCodeSQLExceptionTranslator,按数据库错误码翻译
     *                (MySQL 错误码 1062 = Duplicate entry) → org.springframework.dao.DuplicateKeyException
     */


}