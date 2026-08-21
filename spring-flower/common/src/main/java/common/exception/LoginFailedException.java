package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginFailedException extends BaseException {

    public LoginFailedException(String msg) {
        super(msg);
    }

}
