package common.enums;

public enum OperationEnum {
     /**
     * C创建操作
     */
    CREATE("CREATE"),
    /**
     * R查询操作
     */
    READ("READ"),
    /**
     * U更新操作
     */
    UPDATE("UPDATE"),
    /**
     * D删除操作
     */
    DELETE("DELETE");
     /**
     * 操作类型
     */
    private String operation;

    OperationEnum(String operation) {
        this.operation = operation;
    }

}
