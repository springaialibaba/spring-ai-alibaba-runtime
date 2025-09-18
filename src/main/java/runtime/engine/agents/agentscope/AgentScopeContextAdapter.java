package runtime.engine.agents.agentscope;

import runtime.engine.schemas.context.Context;
import runtime.engine.schemas.agent.Message;
import runtime.engine.memory.model.MessageType;
import runtime.engine.schemas.agent.TextContent;
import runtime.engine.schemas.agent.DataContent;
import runtime.engine.schemas.agent.FunctionCall;
import runtime.engine.schemas.agent.FunctionCallOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AgentScope上下文适配器
 * 对应Python版本的agentscope_agent/agent.py中的AgentScopeContextAdapter类
 */
public class AgentScopeContextAdapter {
    
    private final Context context;
    private final Map<String, Object> attributes;
    
    // 适配后的属性
    private Object toolkit;
    private Object model;
    private Object memory;
    private Object newMessage;
    private Object formatter;
    
    public AgentScopeContextAdapter(Context context, Map<String, Object> attributes) {
        this.context = context;
        this.attributes = attributes;
    }
    
    /**
     * 初始化适配器
     * 对应Python版本的initialize方法
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 适配模型和格式化器
                adaptModel();
                
                // 适配内存
                adaptMemory();
                
                // 适配新消息
                adaptNewMessage();
                
                // 适配工具
                adaptTools();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize AgentScope context adapter", e);
            }
        });
    }
    
    /**
     * 适配内存
     * 对应Python版本的adapt_memory方法
     */
    private void adaptMemory() {
        // 这里应该根据具体的AgentScope Java实现来适配内存
        // 暂时用占位符实现
        this.memory = new Object();
        
        // 构建上下文消息
        List<Object> messages = new ArrayList<>();
        List<Message> sessionMessages = context.getSession().getMessages();
        
        // 排除最后一条消息
        for (int i = 0; i < sessionMessages.size() - 1; i++) {
            Message message = sessionMessages.get(i);
            messages.add(convertMessage(message));
        }
        
        // 加载到内存中
        // 这里需要根据具体的AgentScope内存实现来设置
    }
    
    /**
     * 转换消息
     * 对应Python版本的converter方法
     */
    private Map<String, Object> convertMessage(Message message) {
        Map<String, Object> result = new HashMap<>();
        
        // 设置角色
        String role = message.getRole();
        if (role == null || (!role.equals("user") && !role.equals("system") && !role.equals("assistant"))) {
            role = "user";
        }
        result.put("name", message.getRole());
        result.put("role", role);
        
        // 处理内容
        if (MessageType.PLUGIN_CALL.name().equals(message.getType())) {
            // 处理插件调用
            if (message.getContent() != null && !message.getContent().isEmpty()) {
                DataContent dataContent = (DataContent) message.getContent().get(0);
                Map<String, Object> data = dataContent.getData();
                
                Map<String, Object> toolUseBlock = new HashMap<>();
                toolUseBlock.put("type", "tool_use");
                toolUseBlock.put("id", data.get("call_id"));
                toolUseBlock.put("name", message.getRole());
                toolUseBlock.put("input", data.get("arguments"));
                
                result.put("content", List.of(toolUseBlock));
            }
        } else if (MessageType.PLUGIN_CALL_OUTPUT.name().equals(message.getType())) {
            // 处理插件调用输出
            if (message.getContent() != null && !message.getContent().isEmpty()) {
                DataContent dataContent = (DataContent) message.getContent().get(0);
                Map<String, Object> data = dataContent.getData();
                
                Map<String, Object> toolResultBlock = new HashMap<>();
                toolResultBlock.put("type", "tool_result");
                toolResultBlock.put("id", data.get("call_id"));
                toolResultBlock.put("name", message.getRole());
                toolResultBlock.put("output", data.get("output"));
                
                result.put("content", List.of(toolResultBlock));
            }
        } else {
            // 处理普通消息
            if (message.getContent() != null && !message.getContent().isEmpty()) {
                TextContent textContent = (TextContent) message.getContent().get(0);
                result.put("content", textContent.getText());
            } else {
                result.put("content", "");
            }
        }
        
        return result;
    }
    
    /**
     * 适配新消息
     * 对应Python版本的adapt_new_message方法
     */
    private void adaptNewMessage() {
        Message lastMessage = context.getSession().getLastMessage();
        if (lastMessage != null) {
            this.newMessage = convertMessage(lastMessage);
        }
    }
    
    /**
     * 适配模型
     * 对应Python版本的adapt_model方法
     */
    private void adaptModel() {
        this.model = attributes.get("model");
        
        // 获取格式化器
        Object formatter = attributes.get("agent_config");
        if (formatter instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) formatter;
            this.formatter = config.get("formatter");
        }
        
        // 根据模型类型设置默认格式化器
        if (this.formatter == null) {
            // 这里应该根据具体的模型类型来设置格式化器
            // 暂时用占位符实现
            this.formatter = new Object();
        }
    }
    
    /**
     * 适配工具
     * 对应Python版本的adapt_tools方法
     */
    private void adaptTools() {
        // 获取工具包配置
        Object toolkitConfig = attributes.get("agent_config");
        if (toolkitConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) toolkitConfig;
            this.toolkit = config.get("toolkit");
        }
        
        if (this.toolkit == null) {
            this.toolkit = new Object(); // 默认工具包
        }
        
        // 获取工具列表
        List<Object> tools = (List<Object>) attributes.get("tools");
        if (tools == null || tools.isEmpty()) {
            return;
        }
        
        // 处理激活的工具
        List<String> activateTools = context.getActivateTools();
        if (activateTools != null && !activateTools.isEmpty()) {
            // 只添加激活的工具
            for (String toolName : activateTools) {
                // 这里需要根据具体的工具实现来添加工具
                // 暂时用占位符实现
            }
        } else {
            // 懒加载工具
            // 这里需要根据具体的工具设置逻辑来实现
            // 暂时用占位符实现
        }
    }
    
    // Getter方法
    public Object getToolkit() {
        return toolkit;
    }
    
    public Object getModel() {
        return model;
    }
    
    public Object getMemory() {
        return memory;
    }
    
    public Object getNewMessage() {
        return newMessage;
    }
    
    public Object getFormatter() {
        return formatter;
    }
}
