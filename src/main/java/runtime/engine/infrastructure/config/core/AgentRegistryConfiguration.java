package runtime.engine.infrastructure.config.core;

import runtime.engine.infrastructure.external.registry.AgentRegistry;
import runtime.engine.infrastructure.external.registry.AgentRegistryService;
import io.a2a.spec.AgentCard;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AgentRegistryConfiguration {
    
    @Bean
    @ConditionalOnBean(value = {AgentRegistry.class})
    public AgentRegistryService agentRegistryService(AgentRegistry agentRegistry,
                                                     AgentCard agentCard) {
        return new AgentRegistryService(agentRegistry, agentCard);
    }
    
}
