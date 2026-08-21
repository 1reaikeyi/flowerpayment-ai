import common.enums.OperationEnum;
import start.oparation.OperationType;
import org.junit.jupiter.api.Test;

public class Get {
    @Test
    public void test() {
        System.out.println(OperationType.ok(OperationEnum.UPDATE.name(), 1));
    }
}
