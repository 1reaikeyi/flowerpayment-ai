package common.enums;

public enum OperationErrorEnum {

    /**
     * C创建操作
     */
    CREATE_ERROR("CREATE_ERROR"),
    /**
     * R查询操作
     */
    READ_ERROR("READ_ERROR"),
    /**
     * U更新操作
     */
    UPDATE_ERROR("UPDATE_ERROR"),
    /**
     * D删除操作
     */
    DELETE_ERROR("DELETE_ERROR");
    /**
     * 操作类型
     */
    private String operation;

    OperationErrorEnum(String operation) {
        this.operation = operation;
    }
}
