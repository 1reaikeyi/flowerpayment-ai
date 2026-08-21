package common.exception;

import lombok.NoArgsConstructor;

/**
 * 密码错误异常
 */
@NoArgsConstructor
public class PasswordErrorException extends BaseException {


    public PasswordErrorException(String msg) {
        super(msg);
    }

}
