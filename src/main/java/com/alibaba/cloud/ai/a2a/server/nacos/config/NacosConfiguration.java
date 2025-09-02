package com.alibaba.cloud.ai.a2a.server.nacos.config;

import com.alibaba.cloud.ai.a2a.server.config.NewAgentConfigProperties;
import com.alibaba.cloud.ai.a2a.server.registry.AgentRegistryConfiguration;
import com.alibaba.cloud.ai.a2a.server.ServerAgentConfiguration;
import com.alibaba.cloud.ai.a2a.server.nacos.NacosAgentRegistry;
import com.alibaba.cloud.ai.a2a.server.registry.AgentRegistry;
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
@EnableConfigurationProperties({NacosConfigProperties.class})
@ConditionalOnClass(ServerAgentConfiguration.class)
@AutoConfiguration(after = {ServerAgentConfiguration.class}, before = {AgentRegistryConfiguration.class})
@ConditionalOnProperty(prefix = "ai.agent.registry", value = "type", havingValue = "nacos")
public class NacosConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public A2aMaintainerService a2aMaintainerService(NacosConfigProperties config) throws NacosException {
        return A2aMaintainerFactory.createA2aMaintainerService(config.toNacosProperties());
    }
    
    @Bean
    @ConditionalOnBean(value = {A2aMaintainerService.class, NewAgentConfigProperties.class})
    @ConditionalOnMissingBean
    public AgentRegistry nacosAgentRegistry(A2aMaintainerService a2aMaintainerService,
            NacosConfigProperties nacosConfigProperties) {
        return new NacosAgentRegistry(a2aMaintainerService, nacosConfigProperties);
    }
}
