package runtime.engine.service;

import runtime.engine.schemas.agent.FunctionCall;
import runtime.engine.schemas.agent.FunctionCallOutput;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 工具适配器接口
 * 对应Python版本中的工具适配功能
 */
public interface ToolAdapter {
    
    /**
     * 执行工具
     */
    CompletableFuture<FunctionCallOutput> execute(FunctionCall functionCall);
    
    /**
     * 获取工具Schema
     */
    Map<String, Object> getSchema();
    
    /**
     * 获取工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     */
    String getDescription();
    
    /**
     * 验证参数
     */
    boolean validateParameters(Map<String, Object> parameters);
}
