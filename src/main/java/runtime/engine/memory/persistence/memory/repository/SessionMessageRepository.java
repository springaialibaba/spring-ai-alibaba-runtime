package runtime.engine.memory.persistence.memory.repository;

import runtime.engine.memory.persistence.memory.entity.SessionMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会话消息数据访问接口
 */
@Repository
public interface SessionMessageRepository extends JpaRepository<SessionMessageEntity, Long> {
    
    /**
     * 根据会话ID查找消息
     */
    List<SessionMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * 根据会话ID分页查找消息
     */
    Page<SessionMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId, Pageable pageable);
    
    /**
     * 根据会话ID删除消息
     */
    void deleteBySessionId(String sessionId);
    
    /**
     * 根据会话ID统计消息数量
     */
    long countBySessionId(String sessionId);
    
    /**
     * 获取会话的最新消息
     */
    @Query("SELECT sm FROM SessionMessageEntity sm WHERE sm.sessionId = :sessionId ORDER BY sm.createdAt DESC")
    List<SessionMessageEntity> findLatestBySessionId(@Param("sessionId") String sessionId, Pageable pageable);
}
