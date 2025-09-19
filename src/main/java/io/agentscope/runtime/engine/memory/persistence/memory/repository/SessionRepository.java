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
package io.agentscope.runtime.engine.memory.persistence.memory.repository;

import io.agentscope.runtime.engine.memory.persistence.memory.entity.SessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话数据访问接口
 */
@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    
    /**
     * 根据会话ID查找会话
     */
    Optional<SessionEntity> findBySessionId(String sessionId);
    
    /**
     * 根据用户ID查找会话
     */
    Page<SessionEntity> findByUserIdOrderByLastActivityDesc(String userId, Pageable pageable);
    
    /**
     * 根据用户ID和会话ID查找会话
     */
    Optional<SessionEntity> findByUserIdAndSessionId(String userId, String sessionId);
    
    /**
     * 根据用户ID删除会话
     */
    void deleteByUserId(String userId);
    
    /**
     * 根据会话ID删除会话
     */
    void deleteBySessionId(String sessionId);
    
    /**
     * 获取用户的所有会话
     */
    List<SessionEntity> findByUserIdOrderByLastActivityDesc(String userId);
    
    /**
     * 获取所有会话
     */
    @Query("SELECT s FROM SessionEntity s ORDER BY s.lastActivity DESC")
    List<SessionEntity> findAllOrderByLastActivityDesc();
    
    /**
     * 根据用户ID统计会话数量
     */
    long countByUserId(String userId);
    
    /**
     * 检查会话是否存在
     */
    boolean existsBySessionId(String sessionId);
    
    /**
     * 检查用户和会话是否存在
     */
    boolean existsByUserIdAndSessionId(String userId, String sessionId);
}
