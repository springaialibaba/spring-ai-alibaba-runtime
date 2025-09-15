package runtime.sandbox.manager.model;

/**
 * 沙箱类型枚举
 */
public enum SandboxType {
    BASE("base"),
    BROWSER("browser"),
    FILESYSTEM("filesystem"),
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