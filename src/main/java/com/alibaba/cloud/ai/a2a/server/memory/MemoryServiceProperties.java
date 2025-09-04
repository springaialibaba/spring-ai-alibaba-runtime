package com.alibaba.cloud.ai.a2a.server.memory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 记忆服务配置属性
 */
@ConfigurationProperties(prefix = "memory.service")
public class MemoryServiceProperties {
    
    /**
     * 记忆服务类型：memory 或 redis
     */
    private String type = "memory";
    
    /**
     * 是否自动启动记忆服务
     */
    private boolean autoStart = true;
    
    /**
     * 是否在启动时执行健康检查
     */
    private boolean healthCheckOnStart = true;
    
    /**
     * 默认会话ID
     */
    private String defaultSessionId = "default";
    
    /**
     * 搜索时的默认top_k值
     */
    private int defaultTopK = 5;
    
    /**
     * 分页时的默认页面大小
     */
    private int defaultPageSize = 10;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    public boolean isHealthCheckOnStart() {
        return healthCheckOnStart;
    }
    
    public void setHealthCheckOnStart(boolean healthCheckOnStart) {
        this.healthCheckOnStart = healthCheckOnStart;
    }
    
    public String getDefaultSessionId() {
        return defaultSessionId;
    }
    
    public void setDefaultSessionId(String defaultSessionId) {
        this.defaultSessionId = defaultSessionId;
    }
    
    public int getDefaultTopK() {
        return defaultTopK;
    }
    
    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }
    
    public int getDefaultPageSize() {
        return defaultPageSize;
    }
    
    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
}
