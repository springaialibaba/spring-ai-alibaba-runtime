package runtime.engine.service;

import runtime.engine.schemas.agent.FunctionCall;
import runtime.engine.schemas.agent.FunctionCallOutput;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 工具管理器接口
 * 对应Python版本中的工具管理功能
 */
public interface ToolManager {
    
    /**
     * 注册工具
     */
    void registerTool(String name, ToolAdapter adapter);
    
    /**
     * 取消注册工具
     */
    void unregisterTool(String name);
    
    /**
     * 执行工具调用
     */
    CompletableFuture<FunctionCallOutput> executeTool(FunctionCall functionCall);
    
    /**
     * 获取可用工具列表
     */
    List<ToolInfo> getAvailableTools();
    
    /**
     * 获取工具信息
     */
    ToolInfo getToolInfo(String name);
    
    /**
     * 检查工具是否存在
     */
    boolean toolExists(String name);
    
    /**
     * 获取工具Schema
     */
    Map<String, Object> getToolSchema(String name);
    
    /**
     * 获取所有工具Schema
     */
    Map<String, Map<String, Object>> getAllToolSchemas();
}
