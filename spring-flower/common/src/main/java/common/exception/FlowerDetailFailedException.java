package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FlowerDetailFailedException extends BaseException {
    public FlowerDetailFailedException(String msg) {
        super(msg);
    }
}
