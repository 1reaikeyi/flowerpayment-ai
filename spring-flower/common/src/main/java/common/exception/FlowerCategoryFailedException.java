package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FlowerCategoryFailedException extends BaseException {
    public FlowerCategoryFailedException(String msg) {
        super(msg);
    }
}
