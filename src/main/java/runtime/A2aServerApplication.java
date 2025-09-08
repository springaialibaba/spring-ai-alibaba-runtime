package runtime;

import runtime.infrastructure.config.agent.AgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AgentProperties.class)
public class A2aServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(A2aServerApplication.class, args);
    }

}
