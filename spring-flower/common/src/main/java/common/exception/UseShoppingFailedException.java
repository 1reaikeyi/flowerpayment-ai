package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UseShoppingFailedException extends BaseException {

    public UseShoppingFailedException(String msg) {
        super(msg);
    }

}
