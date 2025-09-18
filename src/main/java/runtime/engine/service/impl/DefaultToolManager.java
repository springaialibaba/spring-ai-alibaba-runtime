package runtime.engine.service.impl;

import runtime.engine.service.ToolAdapter;
import runtime.engine.service.ToolInfo;
import runtime.engine.service.ToolManager;
import runtime.engine.schemas.agent.FunctionCall;
import runtime.engine.schemas.agent.FunctionCallOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认工具管理器实现
 * 对应Python版本中的工具管理功能
 */
public class DefaultToolManager implements ToolManager {
    
    private final Map<String, ToolAdapter> tools;
    private final Map<String, ToolInfo> toolInfos;
    
    public DefaultToolManager() {
        this.tools = new ConcurrentHashMap<>();
        this.toolInfos = new ConcurrentHashMap<>();
    }
    
    @Override
    public void registerTool(String name, ToolAdapter adapter) {
        if (name == null || adapter == null) {
            throw new IllegalArgumentException("Tool name and adapter cannot be null");
        }
        
        tools.put(name, adapter);
        
        // 创建工具信息
        ToolInfo toolInfo = new ToolInfo(
            name,
            adapter.getDescription(),
            adapter.getSchema(),
            "mcp_server",
            "basic"
        );
        toolInfos.put(name, toolInfo);
    }
    
    @Override
    public void unregisterTool(String name) {
        tools.remove(name);
        toolInfos.remove(name);
    }
    
    @Override
    public CompletableFuture<FunctionCallOutput> executeTool(FunctionCall functionCall) {
        if (functionCall == null || functionCall.getName() == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Function call and name cannot be null")
            );
        }
        
        ToolAdapter adapter = tools.get(functionCall.getName());
        if (adapter == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Tool not found: " + functionCall.getName())
            );
        }
        
        return adapter.execute(functionCall);
    }
    
    @Override
    public List<ToolInfo> getAvailableTools() {
        return new ArrayList<>(toolInfos.values());
    }
    
    @Override
    public ToolInfo getToolInfo(String name) {
        return toolInfos.get(name);
    }
    
    @Override
    public boolean toolExists(String name) {
        return tools.containsKey(name);
    }
    
    @Override
    public Map<String, Object> getToolSchema(String name) {
        ToolInfo toolInfo = toolInfos.get(name);
        return toolInfo != null ? toolInfo.getSchema() : null;
    }
    
    @Override
    public Map<String, Map<String, Object>> getAllToolSchemas() {
        Map<String, Map<String, Object>> schemas = new HashMap<>();
        for (Map.Entry<String, ToolInfo> entry : toolInfos.entrySet()) {
            schemas.put(entry.getKey(), entry.getValue().getSchema());
        }
        return schemas;
    }
}
