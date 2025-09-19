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
 */
public class AgentScopeAgent extends BaseAgent {
    
    private final Map<String, Object> attributes;
    private Object agentInstance;
    private List<Object> tools;
    
    public AgentScopeAgent(String name, Object model, List<Object> tools, 
                          AgentConfig agentConfig, Class<?> agentBuilder) {
        super(name, agentConfig);
        
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        
        if (agentBuilder == null) {
            agentBuilder = Object.class;
        }
        
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
                AgentScopeContextAdapter adapter = new AgentScopeContextAdapter(context, attributes);
                
                CompletableFuture<Void> initFuture = adapter.initialize();
                initFuture.thenRun(() -> {
                    try {
                        buildAgent(adapter);
                        
                        String threadId = "pipeline" + UUID.randomUUID().toString();
                        
                        CompletableFuture<Void> executionFuture = executeAgentAsync(adapter, threadId);
                        
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
    
    private void buildAgent(AgentScopeContextAdapter adapter) {
        this.agentInstance = new Object();
        
        registerHooks();
    }
    
    private void registerHooks() {
    }
    
    private CompletableFuture<Void> executeAgentAsync(AgentScopeContextAdapter adapter, String threadId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException("Agent execution failed", e);
            }
        });
    }
    
    private void processMessageStream(String threadId, reactor.core.publisher.FluxSink<Event> sink) {
        ThreadMessageManager messageManager = ThreadMessageManager.getInstance();
        
        messageManager.clearMessages(threadId);
        
        Message message = new Message();
        message.setType(MessageType.MESSAGE.name());
        message.setRole("assistant");
        sink.next(message.inProgress());
        
        messageManager.getMessageStream(threadId)
            .subscribe(
                msg -> {
                    if (msg != null) {
                        processMessage(msg, sink);
                    }
                },
                error -> sink.error(error),
                () -> {
                    sink.next(message.completed());
                }
            );
    }
    
    private void processMessage(Object msg, reactor.core.publisher.FluxSink<Event> sink) {
        try {
            if (msg instanceof String) {
                String content = (String) msg;
                TextContent textContent = new TextContent();
                textContent.setDelta(true);
                textContent.setText(content);
                sink.next(textContent);
            }
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
