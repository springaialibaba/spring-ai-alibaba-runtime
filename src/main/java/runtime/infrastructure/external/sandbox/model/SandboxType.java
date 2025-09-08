package runtime.infrastructure.external.sandbox.model;

/**
 * 沙箱类型枚举
 */
public enum SandboxType {
    BASE("base"),
    PYTHON("python"),
    NODE("node"),
    JAVA("java");

    private final String typeName;

    SandboxType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}