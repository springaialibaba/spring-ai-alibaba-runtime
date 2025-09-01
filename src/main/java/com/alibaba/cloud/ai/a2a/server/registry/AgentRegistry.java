package com.alibaba.cloud.ai.a2a.server.registry;

import com.alibaba.cloud.ai.a2a.server.config.AgentConfigProperties;

/**
 *
 *
 * @author xiweng.yy
 */
public interface AgentRegistry {
    
    void register(AgentConfigProperties.AgentCardInfo agentCardInfo);
}
