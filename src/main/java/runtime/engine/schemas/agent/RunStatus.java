package runtime.engine.schemas.agent;

/**
 * 运行状态枚举
 * 对应Python版本的agent_schemas.py中的RunStatus类
 */
public class RunStatus {
    
    public static final String CREATED = "created";
    public static final String IN_PROGRESS = "in_progress";
    public static final String COMPLETED = "completed";
    public static final String CANCELED = "canceled";
    public static final String FAILED = "failed";
    public static final String REJECTED = "rejected";
    public static final String UNKNOWN = "unknown";
    
    private RunStatus() {
        // 工具类，不允许实例化
    }
}
