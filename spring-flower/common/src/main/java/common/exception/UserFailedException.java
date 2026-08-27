package common.exception;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class UserFailedException extends BaseException {


    public UserFailedException(String msg) {
        super(msg);
    }

}
