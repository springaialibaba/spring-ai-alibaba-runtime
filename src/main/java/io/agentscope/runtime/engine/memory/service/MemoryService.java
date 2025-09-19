package io.agentscope.runtime.engine.memory.service;

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
import io.agentscope.runtime.engine.memory.model.Message;
import io.agentscope.runtime.engine.shared.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 内存服务接口
 * 用于存储和检索长期记忆，支持数据库或内存存储
 * 内存按用户ID组织，支持两种管理策略：
 * 1. 按会话ID分组的消息（会话ID在用户ID下）
 * 2. 仅按用户ID分组的消息
 */
public interface MemoryService extends Service {
    
    /**
     * 添加消息到内存服务
     * 
     * @param userId 用户ID
     * @param messages 要添加的消息列表
     * @param sessionId 可选的会话ID
     * @return CompletableFuture<Void> 异步添加结果
     */
    CompletableFuture<Void> addMemory(String userId, List<Message> messages, Optional<String> sessionId);
    
    /**
     * 从内存服务搜索消息
     * 
     * @param userId 用户ID
     * @param messages 用户查询或带历史消息的查询，都是消息列表格式
     * @param filters 用于搜索内存的过滤器
     * @return CompletableFuture<List<Message>> 异步搜索结果
     */
    CompletableFuture<List<Message>> searchMemory(String userId, List<Message> messages, Optional<Map<String, Object>> filters);
    
    /**
     * 列出指定用户的内存项，支持分页等过滤器
     * 
     * @param userId 用户ID
     * @param filters 内存项的过滤器，如page_num, page_size等
     * @return CompletableFuture<List<Message>> 异步列表结果
     */
    CompletableFuture<List<Message>> listMemory(String userId, Optional<Map<String, Object>> filters);
    
    /**
     * 删除指定用户的内存项
     * 
     * @param userId 用户ID
     * @param sessionId 可选的会话ID，如果提供则只删除该会话的消息，否则删除用户的所有消息
     * @return CompletableFuture<Void> 异步删除结果
     */
    CompletableFuture<Void> deleteMemory(String userId, Optional<String> sessionId);
    
    /**
     * 获取所有用户列表
     * 
     * @return CompletableFuture<List<String>> 异步用户列表结果
     */
    CompletableFuture<List<String>> getAllUsers();
}
