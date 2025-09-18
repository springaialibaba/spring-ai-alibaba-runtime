package runtime.engine.memory.persistence.memory.repository;

import runtime.engine.memory.persistence.memory.entity.SessionEntity;
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
