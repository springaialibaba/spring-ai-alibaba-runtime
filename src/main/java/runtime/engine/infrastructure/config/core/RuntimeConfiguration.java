package runtime.engine.infrastructure.config.core;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(RuntimeConfigProperties.class)
@PropertySource(value = "classpath:application-runtime.yml", factory = YamlPropertySourceFactory.class)
public class RuntimeConfiguration {
}
