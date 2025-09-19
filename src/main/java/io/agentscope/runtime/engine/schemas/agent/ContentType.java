package io.agentscope.runtime.engine.schemas.agent;

/**
 * 内容类型常量类
 * 对应Python版本的agent_schemas.py中的ContentType类
 */
public class ContentType {
    
    public static final String TEXT = "text";
    public static final String DATA = "data";
    public static final String IMAGE = "image";
    public static final String AUDIO = "audio";
    
    private ContentType() {
        // 工具类，不允许实例化
    }
}
