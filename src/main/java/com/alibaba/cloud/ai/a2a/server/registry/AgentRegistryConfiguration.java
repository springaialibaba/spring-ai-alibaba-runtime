package com.alibaba.cloud.ai.a2a.server.registry;

import io.a2a.spec.AgentCard;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class AgentRegistryConfiguration {
    
    @Bean
    @ConditionalOnBean(value = {AgentRegistry.class})
    public AgentRegistryService agentRegistryService(AgentRegistry agentRegistry,
            AgentCard agentCard) {
        return new AgentRegistryService(agentRegistry, agentCard);
    }
    
}
