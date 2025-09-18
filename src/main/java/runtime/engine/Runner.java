package runtime.engine;

import reactor.core.publisher.Flux;
import runtime.engine.agents.Agent;
import runtime.engine.memory.context.ContextManager;
import runtime.engine.schemas.agent.AgentRequest;
import runtime.engine.schemas.agent.Event;
import runtime.engine.schemas.context.Context;
import runtime.engine.schemas.context.Session;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 运行器类
 * 对应Python版本的runner.py中的Runner类
 */
public class Runner implements AutoCloseable {
    
    private final Agent agent;
    private final ContextManager contextManager;
    private final String sessionId;
    private final String userId;
    
    public Runner(Agent agent, ContextManager contextManager) {
        this(agent, contextManager, "default_user", UUID.randomUUID().toString());
    }
    
    public Runner(Agent agent, ContextManager contextManager, String userId, String sessionId) {
        this.agent = agent;
        this.contextManager = contextManager;
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    /**
     * 流式查询
     * 对应Python版本的stream_query方法
     */
    public Flux<Event> streamQuery(AgentRequest request) {
        return Flux.create(sink -> {
            try {
                // 创建或获取会话
                runtime.engine.memory.model.Session memorySession = contextManager.composeSession(userId, sessionId).join();
                
                // 转换为上下文会话
                Session session = new Session();
                session.setId(memorySession.getId());
                session.setUserId(memorySession.getUserId());
                
                // 创建上下文
                Context context = new Context();
                context.setUserId(userId);
                context.setSession(session);
                context.setRequest(request);
                context.setAgent(agent);
                
                // 设置当前消息（从请求中获取）
                if (request.getInput() != null && !request.getInput().isEmpty()) {
                    context.setCurrentMessages(request.getInput());
                }
                
                // 执行Agent
                CompletableFuture<Flux<Event>> agentFuture = agent.runAsync(context);
                
                agentFuture.thenAccept(eventFlux -> {
                    eventFlux.subscribe(
                        event -> sink.next(event),
                        error -> sink.error(error),
                        () -> sink.complete()
                    );
                }).exceptionally(throwable -> {
                    sink.error(throwable);
                    return null;
                });
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    /**
     * 非流式查询
     * 对应Python版本的query方法
     */
    public CompletableFuture<Event> query(AgentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            List<Event> events = streamQuery(request).collectList().block();
            if (events != null && !events.isEmpty()) {
                return events.get(events.size() - 1);
            }
            return null;
        });
    }
    
    @Override
    public void close() {
        // 清理资源
        try {
            // ContextManager没有clearContext方法，这里可以添加其他清理逻辑
            // contextManager.clearContext(sessionId).join();
        } catch (Exception e) {
            // 忽略清理错误
        }
    }
}
