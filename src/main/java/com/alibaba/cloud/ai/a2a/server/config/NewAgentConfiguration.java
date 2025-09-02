package com.alibaba.cloud.ai.a2a.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(NewAgentConfigProperties.class)
@PropertySource(value = "classpath:application-new-agent.yml", factory = YamlPropertySourceFactory.class)
public class NewAgentConfiguration {
}
