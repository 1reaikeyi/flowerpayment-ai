package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UseShoppingBusinessException extends BaseException {

    public UseShoppingBusinessException(String msg) {
        super(msg);
    }

}
