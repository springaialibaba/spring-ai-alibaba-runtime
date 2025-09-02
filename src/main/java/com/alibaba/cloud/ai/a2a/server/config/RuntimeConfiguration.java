package com.alibaba.cloud.ai.a2a.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(RuntimeConfigProperties.class)
@PropertySource(value = "classpath:application-runtime.yml", factory = YamlPropertySourceFactory.class)
public class RuntimeConfiguration {
}
