package common.exception;

import lombok.NoArgsConstructor;

/**
 * 账号不存在异常
 */
@NoArgsConstructor
public class UserException extends BaseException {


    public UserException(String msg) {
        super(msg);
    }

}
