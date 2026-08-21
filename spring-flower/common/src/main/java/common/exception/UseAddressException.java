package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UseAddressException extends BaseException {

    public UseAddressException(String msg) {
        super(msg);
    }

}
