package runtime.infrastructure.config.agent;

import runtime.infrastructure.config.core.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(AgentProperties.class)
@PropertySource(value = "classpath:application-agent.yml", factory = YamlPropertySourceFactory.class)
public class AgentConfig {
}
