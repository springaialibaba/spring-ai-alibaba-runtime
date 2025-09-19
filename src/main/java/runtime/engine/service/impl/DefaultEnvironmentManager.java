package runtime.engine.service.impl;

import runtime.engine.service.EnvironmentManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 默认环境管理器实现
 * 对应Python版本的environment_manager.py中的默认实现
 */
public class DefaultEnvironmentManager implements EnvironmentManager {
    
    private final Map<String, String> environmentVariables;
    private boolean initialized = false;
    
    public DefaultEnvironmentManager() {
        this.environmentVariables = new HashMap<>();
    }
    
    @Override
    public String getEnvironmentVariable(String key) {
        // 首先从系统环境变量获取
        String systemValue = System.getenv(key);
        if (systemValue != null) {
            return systemValue;
        }
        
        // 然后从本地存储获取
        return environmentVariables.get(key);
    }
    
    @Override
    public void setEnvironmentVariable(String key, String value) {
        environmentVariables.put(key, value);
    }
    
    @Override
    public Map<String, String> getAllEnvironmentVariables() {
        Map<String, String> allVars = new HashMap<>();
        
        // 添加系统环境变量
        allVars.putAll(System.getenv());
        
        // 添加本地环境变量
        allVars.putAll(environmentVariables);
        
        return allVars;
    }
    
    @Override
    public boolean isEnvironmentAvailable() {
        return initialized;
    }
    
    @Override
    public CompletableFuture<Void> initializeEnvironment() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 初始化环境
                // 这里可以添加具体的初始化逻辑
                // 比如检查必要的环境变量、创建必要的目录等
                
                // 设置一些默认的环境变量
                if (!environmentVariables.containsKey("AGENT_RUNTIME_HOME")) {
                    environmentVariables.put("AGENT_RUNTIME_HOME", System.getProperty("user.home") + "/.agent-runtime");
                }
                
                if (!environmentVariables.containsKey("AGENT_LOG_LEVEL")) {
                    environmentVariables.put("AGENT_LOG_LEVEL", "INFO");
                }
                
                initialized = true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize environment", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> cleanupEnvironment() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 清理环境
                // 这里可以添加具体的清理逻辑
                environmentVariables.clear();
                initialized = false;
            } catch (Exception e) {
                throw new RuntimeException("Failed to cleanup environment", e);
            }
        });
    }
    
    @Override
    public Map<String, Object> getEnvironmentInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("initialized", initialized);
        info.put("environment_variables_count", environmentVariables.size());
        info.put("system_properties", System.getProperties());
        info.put("java_version", System.getProperty("java.version"));
        info.put("os_name", System.getProperty("os.name"));
        info.put("os_version", System.getProperty("os.version"));
        return info;
    }
}
