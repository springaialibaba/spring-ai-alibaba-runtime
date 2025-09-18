package runtime.engine.agents.agentscope;

import reactor.core.publisher.Flux;
import runtime.engine.agents.BaseAgent;
import runtime.engine.agents.config.AgentConfig;
import runtime.engine.schemas.context.Context;
import runtime.engine.schemas.agent.Event;
import runtime.engine.schemas.agent.Message;
import runtime.engine.memory.model.MessageType;
import runtime.engine.schemas.agent.TextContent;
import runtime.engine.schemas.agent.DataContent;
import runtime.engine.schemas.agent.FunctionCall;
import runtime.engine.schemas.agent.FunctionCallOutput;
import runtime.engine.agents.agentscope.hooks.ThreadMessageManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AgentScope Agent实现
 * 对应Python版本的agentscope_agent/agent.py中的AgentScopeAgent类
 */
public class AgentScopeAgent extends BaseAgent {
    
    private final Map<String, Object> attributes;
    private Object agentInstance;
    private List<Object> tools;
    
    public AgentScopeAgent(String name, Object model, List<Object> tools, 
                          AgentConfig agentConfig, Class<?> agentBuilder) {
        super(name, agentConfig);
        
        // 验证模型类型
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        
        // 设置默认agent_builder
        if (agentBuilder == null) {
            // 这里应该使用ReActAgent类，暂时用Object代替
            agentBuilder = Object.class;
        }
        
        // 替换名称如果不存在
        if (agentConfig.get("name") == null || agentConfig.get("name").toString().isEmpty()) {
            agentConfig.set("name", name);
        }
        
        this.attributes = Map.of(
            "model", model,
            "tools", tools != null ? tools : List.of(),
            "agent_config", agentConfig,
            "agent_builder", agentBuilder
        );
        
        this.agentInstance = null;
        this.tools = tools;
    }
    
    @Override
    public AgentScopeAgent copy() {
        return new AgentScopeAgent(
            getName(),
            attributes.get("model"),
            (List<Object>) attributes.get("tools"),
            getConfig(),
            (Class<?>) attributes.get("agent_builder")
        );
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                // 创建上下文适配器
                AgentScopeContextAdapter adapter = new AgentScopeContextAdapter(context, attributes);
                
                // 初始化适配器
                CompletableFuture<Void> initFuture = adapter.initialize();
                initFuture.thenRun(() -> {
                    try {
                        // 构建Agent实例
                        buildAgent(adapter);
                        
                        // 创建消息流
                        String threadId = "pipeline" + UUID.randomUUID().toString();
                        
                        // 启动Agent执行
                        CompletableFuture<Void> executionFuture = executeAgentAsync(adapter, threadId);
                        
                        // 处理消息流
                        processMessageStream(threadId, sink);
                        
                        executionFuture.whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                sink.error(throwable);
                            } else {
                                sink.complete();
                            }
                        });
                        
                    } catch (Exception e) {
                        sink.error(e);
                    }
                });
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    /**
     * 构建Agent实例
     * 对应Python版本的build方法
     */
    private void buildAgent(AgentScopeContextAdapter adapter) {
        // 这里应该根据具体的AgentScope Java实现来构建Agent
        // 暂时用占位符实现
        this.agentInstance = new Object();
        
        // 注册hooks
        registerHooks();
    }
    
    /**
     * 注册hooks
     * 对应Python版本中的register_instance_hook
     */
    private void registerHooks() {
        // 注册pre_print hook
        // 这里应该根据具体的AgentScope Java实现来注册hooks
    }
    
    /**
     * 异步执行Agent
     * 对应Python版本中的run方法
     */
    private CompletableFuture<Void> executeAgentAsync(AgentScopeContextAdapter adapter, String threadId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 在独立线程中运行Agent
                // 这里应该调用具体的AgentScope Agent的reply方法
                // 暂时用占位符实现
                Thread.sleep(100); // 模拟执行
            } catch (Exception e) {
                throw new RuntimeException("Agent execution failed", e);
            }
        });
    }
    
    /**
     * 处理消息流
     * 对应Python版本中的消息流处理逻辑
     */
    private void processMessageStream(String threadId, reactor.core.publisher.FluxSink<Event> sink) {
        // 创建消息流管理器
        ThreadMessageManager messageManager = ThreadMessageManager.getInstance();
        
        // 清理之前的消息
        messageManager.clearMessages(threadId);
        
        // 创建初始消息
        Message message = new Message();
        message.setType(MessageType.MESSAGE.name());
        message.setRole("assistant");
        sink.next(message.inProgress());
        
        // 处理消息流
        messageManager.getMessageStream(threadId)
            .subscribe(
                msg -> {
                    if (msg != null) {
                        processMessage(msg, sink);
                    }
                },
                error -> sink.error(error),
                () -> {
                    // 完成消息
                    sink.next(message.completed());
                }
            );
    }
    
    /**
     * 处理单个消息
     * 对应Python版本中的消息处理逻辑
     */
    private void processMessage(Object msg, reactor.core.publisher.FluxSink<Event> sink) {
        // 这里应该根据具体的消息格式来处理
        // 对应Python版本中的消息处理逻辑
        try {
            // 处理文本内容
            if (msg instanceof String) {
                String content = (String) msg;
                TextContent textContent = new TextContent();
                textContent.setDelta(true);
                textContent.setText(content);
                sink.next(textContent);
            }
            // 处理工具调用
            else if (msg instanceof Map) {
                Map<String, Object> msgMap = (Map<String, Object>) msg;
                String type = (String) msgMap.get("type");
                
                if ("tool_use".equals(type)) {
                    processToolUse(msgMap, sink);
                } else if ("tool_result".equals(type)) {
                    processToolResult(msgMap, sink);
                }
            }
        } catch (Exception e) {
            sink.error(e);
        }
    }
    
    /**
     * 处理工具调用
     */
    private void processToolUse(Map<String, Object> msgMap, reactor.core.publisher.FluxSink<Event> sink) {
        FunctionCall functionCall = new FunctionCall();
        functionCall.setCallId((String) msgMap.get("id"));
        functionCall.setName((String) msgMap.get("name"));
        functionCall.setArguments(msgMap.get("input").toString());
        
        DataContent dataContent = new DataContent();
        dataContent.setData(functionCall.toMap());
        
        Message pluginCallMessage = new Message();
        pluginCallMessage.setType(MessageType.PLUGIN_CALL.name());
        pluginCallMessage.setRole("assistant");
        pluginCallMessage.setContent(List.of(dataContent));
        
        sink.next(pluginCallMessage.completed());
    }
    
    /**
     * 处理工具结果
     */
    private void processToolResult(Map<String, Object> msgMap, reactor.core.publisher.FluxSink<Event> sink) {
        FunctionCallOutput functionCallOutput = new FunctionCallOutput();
        functionCallOutput.setCallId((String) msgMap.get("id"));
        functionCallOutput.setOutput(msgMap.get("output").toString());
        
        DataContent dataContent = new DataContent();
        dataContent.setData(functionCallOutput.toMap());
        
        Message pluginOutputMessage = new Message();
        pluginOutputMessage.setType(MessageType.PLUGIN_CALL_OUTPUT.name());
        pluginOutputMessage.setRole("assistant");
        pluginOutputMessage.setContent(List.of(dataContent));
        
        sink.next(pluginOutputMessage.completed());
    }
}
