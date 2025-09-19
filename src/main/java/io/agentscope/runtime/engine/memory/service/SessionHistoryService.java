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
package io.agentscope.runtime.engine.memory.service;

import io.agentscope.runtime.engine.memory.model.Message;
import io.agentscope.runtime.engine.memory.model.Session;
import io.agentscope.runtime.engine.shared.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 会话历史管理服务接口
 * 定义创建、检索、更新和删除对话会话的标准接口
 */
public interface SessionHistoryService extends Service {
    
    /**
     * 为指定用户创建新会话
     * 
     * @param userId 用户标识符
     * @param sessionId 可选的会话ID，如果为null则自动生成
     * @return CompletableFuture<Session> 异步创建的新会话对象
     */
    CompletableFuture<Session> createSession(String userId, Optional<String> sessionId);
    
    /**
     * 检索特定会话
     * 
     * @param userId 用户标识符
     * @param sessionId 要检索的会话标识符
     * @return CompletableFuture<Optional<Session>> 异步检索结果，如果找到则返回会话对象，否则返回空
     */
    CompletableFuture<Optional<Session>> getSession(String userId, String sessionId);
    
    /**
     * 删除特定会话
     * 
     * @param userId 用户标识符
     * @param sessionId 要删除的会话标识符
     * @return CompletableFuture<Void> 异步删除结果
     */
    CompletableFuture<Void> deleteSession(String userId, String sessionId);
    
    /**
     * 列出指定用户的所有会话
     * 
     * @param userId 用户标识符
     * @return CompletableFuture<List<Session>> 异步会话列表结果
     */
    CompletableFuture<List<Session>> listSessions(String userId);
    
    /**
     * 向特定会话的历史记录追加消息
     * 
     * @param session 要追加消息的会话
     * @param messages 要追加的消息或消息列表
     * @return CompletableFuture<Void> 异步追加结果
     */
    CompletableFuture<Void> appendMessage(Session session, List<Message> messages);
}
