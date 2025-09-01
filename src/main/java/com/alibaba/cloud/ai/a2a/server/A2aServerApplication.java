package com.alibaba.cloud.ai.a2a.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.alibaba.cloud.ai.a2a.server.config.AgentConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(AgentConfigProperties.class)
public class A2aServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(A2aServerApplication.class, args);
    }

}
