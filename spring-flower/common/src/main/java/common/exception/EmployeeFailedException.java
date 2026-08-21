package common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmployeeFailedException extends BaseException {

    public EmployeeFailedException(String msg) {
        super(msg);
    }

}
