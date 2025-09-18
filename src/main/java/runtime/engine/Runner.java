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
     */
    public Flux<Event> streamQuery(AgentRequest request) {
        return Flux.create(sink -> {
            try {
                runtime.engine.memory.model.Session memorySession = contextManager.composeSession(userId, sessionId).join();

                Session session = new Session();
                session.setId(memorySession.getId());
                session.setUserId(memorySession.getUserId());

                Context context = new Context();
                context.setUserId(userId);
                context.setSession(session);
                context.setRequest(request);
                context.setAgent(agent);

                if (request.getInput() != null && !request.getInput().isEmpty()) {
                    context.setCurrentMessages(request.getInput());
                }

                CompletableFuture<Flux<Event>> agentFuture = agent.runAsync(context);

                agentFuture.thenAccept(eventFlux -> {
                    eventFlux.subscribe(
                        sink::next,
                        sink::error,
                        sink::complete
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

    @Override
    public void close() {
        try {
            // 清理资源
        } catch (Exception e) {
            // 忽略清理错误
        }
    }
}