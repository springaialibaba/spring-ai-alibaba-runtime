package runtime.shared.context;

import runtime.domain.memory.model.Message;
import runtime.domain.memory.model.Session;
import runtime.domain.memory.service.MemoryService;
import runtime.domain.memory.service.SessionHistoryService;
import runtime.infrastructure.persistence.memory.service.InMemoryMemoryService;
import runtime.infrastructure.persistence.session.InMemorySessionHistoryService;
import runtime.shared.common.ServiceManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 上下文管理器
 * 管理会话历史和内存服务的组合
 */
public class ContextManager extends ServiceManager {
    
    private final Class<? extends ContextComposer> contextComposerClass;
    private SessionHistoryService sessionHistoryService;
    private MemoryService memoryService;
    
    public ContextManager() {
        this.contextComposerClass = ContextComposer.class;
        this.sessionHistoryService = null;
        this.memoryService = null;
    }
    
    public ContextManager(
            Class<? extends ContextComposer> contextComposerClass,
            SessionHistoryService sessionHistoryService,
            MemoryService memoryService) {
        this.contextComposerClass = contextComposerClass;
        this.sessionHistoryService = sessionHistoryService;
        this.memoryService = memoryService;
    }
    
    @Override
    protected void registerDefaultServices() {
        // 注册上下文管理的默认服务
        this.sessionHistoryService = this.sessionHistoryService != null ? 
                this.sessionHistoryService : new InMemorySessionHistoryService();
        this.memoryService = this.memoryService != null ? 
                this.memoryService : new InMemoryMemoryService();
        
        registerService("session", this.sessionHistoryService);
        registerService("memory", this.memoryService);
    }
    
    /**
     * 组合上下文信息
     * 
     * @param session 会话对象
     * @param requestInput 请求输入消息
     * @return CompletableFuture<Void> 异步组合结果
     */
    public CompletableFuture<Void> composeContext(Session session, List<Message> requestInput) {
        return ContextComposer.compose(
                requestInput,
                session,
                Optional.ofNullable(this.memoryService),
                Optional.ofNullable(this.sessionHistoryService)
        );
    }
    
    /**
     * 组合会话
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return CompletableFuture<Session> 异步会话结果
     */
    public CompletableFuture<Session> composeSession(String userId, String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.sessionHistoryService != null) {
                try {
                    Optional<Session> sessionOpt = this.sessionHistoryService
                            .getSession(userId, sessionId).get();
                    if (sessionOpt.isEmpty()) {
                        throw new RuntimeException("Session " + sessionId + " not found");
                    }
                    return sessionOpt.get();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to get session", e);
                }
            } else {
                return new Session(sessionId, userId, List.of());
            }
        });
    }
    
    /**
     * 追加消息到会话
     * 
     * @param session 会话对象
     * @param eventOutput 事件输出消息
     * @return CompletableFuture<Void> 异步追加结果
     */
    public CompletableFuture<Void> append(Session session, List<Message> eventOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (this.sessionHistoryService != null) {
                    this.sessionHistoryService.appendMessage(session, eventOutput).get();
                }
                if (this.memoryService != null) {
                    this.memoryService.addMemory(session.getUserId(), eventOutput, Optional.of(session.getId())).get();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to append messages", e);
            }
        });
    }
    
    /**
     * 获取会话历史服务
     */
    public SessionHistoryService getSessionHistoryService() {
        return sessionHistoryService;
    }
    
    /**
     * 获取内存服务
     */
    public MemoryService getMemoryService() {
        return memoryService;
    }
}
