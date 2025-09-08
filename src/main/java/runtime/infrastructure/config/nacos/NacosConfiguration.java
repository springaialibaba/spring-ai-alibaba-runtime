package runtime.infrastructure.config.nacos;

import runtime.infrastructure.config.agent.AgentProperties;
import runtime.infrastructure.config.core.AgentRegistryConfiguration;
import runtime.infrastructure.config.agent.ServerAgentConfiguration;
import runtime.infrastructure.external.nacos.NacosAgentRegistry;
import runtime.infrastructure.external.registry.AgentRegistry;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.maintainer.client.ai.A2aMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.A2aMaintainerService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 *
 *
 * @author xiweng.yy
 */
@EnableConfigurationProperties({NacosProperties.class})
@ConditionalOnClass(ServerAgentConfiguration.class)
@AutoConfiguration(after = {ServerAgentConfiguration.class}, before = {AgentRegistryConfiguration.class})
@ConditionalOnProperty(prefix = "ai.agent.registry", value = "type", havingValue = "nacos")
public class NacosConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public A2aMaintainerService a2aMaintainerService(NacosProperties config) throws NacosException {
        return A2aMaintainerFactory.createA2aMaintainerService(config.toNacosProperties());
    }
    
    @Bean
    @ConditionalOnBean(value = {A2aMaintainerService.class, AgentProperties.class})
    @ConditionalOnMissingBean
    public AgentRegistry nacosAgentRegistry(A2aMaintainerService a2aMaintainerService,
            NacosProperties nacosProperties) {
        return new NacosAgentRegistry(a2aMaintainerService, nacosProperties);
    }
}
