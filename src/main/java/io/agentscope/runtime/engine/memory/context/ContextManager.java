/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.runtime.engine.memory.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.agentscope.runtime.engine.memory.model.Message;
import io.agentscope.runtime.engine.memory.model.Session;
import io.agentscope.runtime.engine.memory.persistence.memory.service.InMemoryMemoryService;
import io.agentscope.runtime.engine.memory.persistence.session.InMemorySessionHistoryService;
import io.agentscope.runtime.engine.memory.service.MemoryService;
import io.agentscope.runtime.engine.memory.service.SessionHistoryService;
import io.agentscope.runtime.engine.shared.ServiceManager;

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
            SessionHistoryService sessionHistoryService = getSessionHistoryService();
            if (sessionHistoryService != null) {
                try {
                    Optional<Session> sessionOpt = sessionHistoryService
                            .getSession(userId, sessionId).get();
                    if (sessionOpt.isEmpty()) {
                        // 如果Session不存在，创建一个新的
                        return sessionHistoryService.createSession(userId, Optional.of(sessionId)).get();
                    }
                    return sessionOpt.get();
                } catch (Exception e) {
                    // 如果获取失败，创建一个新的Session
                    try {
                        return sessionHistoryService.createSession(userId, Optional.of(sessionId)).get();
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to get or create session", ex);
                    }
                }
            } else {
                return new Session(sessionId, userId, new ArrayList<>());
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
                SessionHistoryService sessionHistoryService = getSessionHistoryService();
                if (sessionHistoryService != null) {
                    sessionHistoryService.appendMessage(session, eventOutput).get();
                }

                MemoryService memoryService = getMemoryService();
                if (memoryService != null) {
                    memoryService.addMemory(session.getUserId(), eventOutput, Optional.of(session.getId())).get();
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
        return (SessionHistoryService) getService("session");
    }

    /**
     * 获取内存服务
     */
    public MemoryService getMemoryService() {
        return (MemoryService) getService("memory");
    }
}
