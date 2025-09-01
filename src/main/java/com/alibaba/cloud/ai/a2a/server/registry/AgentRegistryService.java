package com.alibaba.cloud.ai.a2a.server.registry;

import com.alibaba.cloud.ai.a2a.server.config.AgentConfigProperties;
import jakarta.annotation.PostConstruct;

/**
 *
 *
 * @author xiweng.yy
 */
public class AgentRegistryService {
    
    private final AgentRegistry agentRegistry;
    
    private final AgentConfigProperties.AgentCardInfo agentCardInfo;
    
    public AgentRegistryService(AgentRegistry agentRegistry, AgentConfigProperties.AgentCardInfo agentCardInfo) {
        this.agentRegistry = agentRegistry;
        this.agentCardInfo = agentCardInfo;
    }
    
    @PostConstruct
    public void register() {
        agentRegistry.register(agentCardInfo);
    }
}
