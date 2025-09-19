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
package io.agentscope.runtime.engine.memory.persistence.session;

import io.agentscope.runtime.engine.memory.model.Message;
import io.agentscope.runtime.engine.memory.model.Session;
import io.agentscope.runtime.engine.memory.service.SessionHistoryService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存实现的会话历史服务
 * 将所有会话数据存储在字典中，适用于开发、测试和不需要持久化的场景
 */
public class InMemorySessionHistoryService implements SessionHistoryService {
    
    private final Map<String, Map<String, Session>> sessions = new ConcurrentHashMap<>();
    
    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Boolean> health() {
        return CompletableFuture.completedFuture(true);
    }
    
    @Override
    public CompletableFuture<Session> createSession(String userId, Optional<String> sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            String sid = sessionId.filter(s -> s != null && !s.trim().isEmpty())
                    .orElse(UUID.randomUUID().toString());
            
            Session session = new Session(sid, userId, new ArrayList<>());
            sessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                    .put(sid, session);
            
            return deepCopy(session);
        });
    }
    
    @Override
    public CompletableFuture<Optional<Session>> getSession(String userId, String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            Session session = sessions.getOrDefault(userId, Collections.emptyMap())
                    .get(sessionId);
            
            if (session == null) {
                session = new Session(sessionId, userId, new ArrayList<>());
                sessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                        .put(sessionId, session);
            }
            
            return Optional.of(deepCopy(session));
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteSession(String userId, String sessionId) {
        return CompletableFuture.runAsync(() -> {
            Map<String, Session> userSessions = sessions.get(userId);
            if (userSessions != null) {
                userSessions.remove(sessionId);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Session>> listSessions(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Session> userSessions = sessions.getOrDefault(userId, Collections.emptyMap());
            
            // 为了提高性能和减少数据传输，返回的会话对象不包含详细的响应历史
            return userSessions.values().stream()
                    .map(this::createSessionWithoutHistory)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public CompletableFuture<Void> appendMessage(Session session, List<Message> messages) {
        return CompletableFuture.runAsync(() -> {
            if (messages == null || messages.isEmpty()) {
                return;
            }
            
            // 更新传入的会话对象
            session.getMessages().addAll(messages);
            
            // 更新内存中的副本
            Map<String, Session> userSessions = sessions.get(session.getUserId());
            if (userSessions != null) {
                Session storageSession = userSessions.get(session.getId());
                if (storageSession != null) {
                    storageSession.getMessages().addAll(messages);
                } else {
                    System.err.println("Warning: Session " + session.getId() + 
                            " not found in storage for append_message.");
                }
            }
        });
    }
    
    /**
     * 创建不包含历史记录的会话副本
     * 
     * @param session 原始会话
     * @return 不包含历史记录的会话副本
     */
    private Session createSessionWithoutHistory(Session session) {
        Session copy = deepCopy(session);
        copy.setMessages(new ArrayList<>());
        return copy;
    }
    
    /**
     * 深拷贝会话对象
     * 
     * @param session 要拷贝的会话
     * @return 深拷贝的会话对象
     */
    private Session deepCopy(Session session) {
        Session copy = new Session();
        copy.setId(session.getId());
        copy.setUserId(session.getUserId());
        copy.setMessages(new ArrayList<>(session.getMessages()));
        return copy;
    }
}
