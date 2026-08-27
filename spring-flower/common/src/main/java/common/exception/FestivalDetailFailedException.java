package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FestivalDetailFailedException extends BaseException{
    public FestivalDetailFailedException(String msg) {
        super(msg);
    }
}
