package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderFailedException extends BaseException {

    public OrderFailedException(String msg) {
        super(msg);
    }

}
