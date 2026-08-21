package common.exception;

import lombok.NoArgsConstructor;

/**
 * 账号被锁定异常
 */
@NoArgsConstructor
public class FlowerFailedException extends BaseException {


    public FlowerFailedException(String msg) {
        super(msg);
    }

}
