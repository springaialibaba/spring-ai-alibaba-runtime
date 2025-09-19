package io.agentscope.runtime.engine.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 环境管理器接口
 * 对应Python版本的environment_manager.py中的EnvironmentManager类
 */
public interface EnvironmentManager {
    
    /**
     * 获取环境变量
     */
    String getEnvironmentVariable(String key);
    
    /**
     * 设置环境变量
     */
    void setEnvironmentVariable(String key, String value);
    
    /**
     * 获取所有环境变量
     */
    Map<String, String> getAllEnvironmentVariables();
    
    /**
     * 检查环境是否可用
     */
    boolean isEnvironmentAvailable();
    
    /**
     * 初始化环境
     */
    CompletableFuture<Void> initializeEnvironment();
    
    /**
     * 清理环境
     */
    CompletableFuture<Void> cleanupEnvironment();
    
    /**
     * 获取环境信息
     */
    Map<String, Object> getEnvironmentInfo();
}
