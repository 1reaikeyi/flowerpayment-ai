package common.exception;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class FlowerFailedException extends BaseException {


    public FlowerFailedException(String msg) {
        super(msg);
    }

}
