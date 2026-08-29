package start.exceptionhandle;

import common.constant.ErrorConstant;
import common.exception.*;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class ExceptionHandle {

    @ExceptionHandler(LoginFailedException.class)
    public Result loginFailedExceptionHandler(LoginFailedException e) {
        return Result.error(ErrorConstant.LOGIN_FAILED+ ", " + e.getMessage() + ", 请检查账号密码");
    }

    @ExceptionHandler(PasswordErrorException.class)
    public Result passwordErrorExceptionHandler(PasswordErrorException e) {
        return Result.error(ErrorConstant.PASSWORD_ERROR+ ", " + e.getMessage() + ", 请重新输入");
    }
    @ExceptionHandler(EmployeeFailedException.class)
    public Result employeeFailedExceptionHandler(EmployeeFailedException e) {
        return Result.error(ErrorConstant.EMPLOYEE_FAILED+ ", " + e.getMessage() + ", 请检查员工信息");
    }
    @ExceptionHandler(FlowerFailedException.class)
    public Result flowerFailedExceptionHandler(FlowerFailedException e) {
        return Result.error(ErrorConstant.FLOWER_FAILED+ ", " + e.getMessage() + ", 请检查花卉信息");
    }

    @ExceptionHandler(FlowerDetailFailedException.class)
    public Result flowerDetailFailedExceptionHandler(FlowerDetailFailedException e) {
        return Result.error(ErrorConstant.FLOWER_DETAIL_FAILED+ ", " + e.getMessage() + ", 请检查花卉详情信息");
    }

    @ExceptionHandler(FlowerCategoryFailedException.class)
    public Result flowerCategoryFailedExceptionHandler(FlowerCategoryFailedException e) {
        return Result.error(ErrorConstant.FLOWER_CATEGORY_FAILED+ ", " + e.getMessage() + ", 请检查花卉目录信息");
    }

    @ExceptionHandler(FestivalFailedException.class)
    public Result festivalFailedExceptionHandler(FestivalFailedException e) {
        return Result.error(ErrorConstant.FESTIVAL_FAILED+ ", " + e.getMessage() + ", 请检查节日信息");
    }

    @ExceptionHandler(FestivalDetailFailedException.class)
    public Result festivalDetailFailedExceptionHandler(FestivalDetailFailedException e) {
        return Result.error(ErrorConstant.FESTIVAL_DETAIL_FAILED+ ", " + e.getMessage() + ", 请检查节日详情信息");
    }


    @ExceptionHandler(UserFailedException.class)
    public Result userFailedExceptionHandler(UserFailedException e) {
        return Result.error(ErrorConstant.USER_FAILED+ ", " + e.getMessage() + ", 请检查用户信息");
    }

    @ExceptionHandler(UseAddressFailedException.class)
    public Result useAddressFailedExceptionHandler(UseAddressFailedException e) {
        return Result.error(ErrorConstant.U_ADDRESS_FAILED+ ", " + e.getMessage() + ", 请检查用户地址信息");
    }

    @ExceptionHandler(UseShoppingFailedException.class)
    public Result useShoppingFailedExceptionHandler(UseShoppingFailedException e) {
        return Result.error(ErrorConstant.U_SHOPPING_FAILED+ ", " + e.getMessage() + ", 请检查用户购物车信息");
    }

    @ExceptionHandler(FlowerOrderFailedException.class)
    public Result flowerOrderFailedExceptionHandler(FlowerOrderFailedException e) {
        return Result.error(ErrorConstant.FLOWER_ORDER_FAILED+ ", " + e.getMessage() + ", 请检查花卉订单信息");
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
