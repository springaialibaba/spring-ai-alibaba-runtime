package runtime.infrastructure.config.core;

import runtime.infrastructure.config.agent.AgentProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "runtime")
public class RuntimeConfigProperties {

    private AgentProperties.Model model;

    public AgentProperties.Model getModel() {
        return model;
    }

    public void setModel(AgentProperties.Model model) {
        this.model = model;
    }
}
