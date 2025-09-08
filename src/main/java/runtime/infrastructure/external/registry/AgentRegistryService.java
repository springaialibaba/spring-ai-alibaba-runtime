package runtime.infrastructure.external.registry;

import io.a2a.spec.AgentCard;
import jakarta.annotation.PostConstruct;

/**
 *
 *
 * @author xiweng.yy
 */
public class AgentRegistryService {

    private final AgentRegistry agentRegistry;

    private final AgentCard agentCard;

    public AgentRegistryService(AgentRegistry agentRegistry, AgentCard agentCard) {
        this.agentRegistry = agentRegistry;
        this.agentCard = agentCard;
    }

    @PostConstruct
    public void register() {
        agentRegistry.register(agentCard);
    }
}
