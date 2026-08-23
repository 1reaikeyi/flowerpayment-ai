package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FlowerOrderFailedException extends BaseException {

    public FlowerOrderFailedException(String msg) {
        super(msg);
    }

}
