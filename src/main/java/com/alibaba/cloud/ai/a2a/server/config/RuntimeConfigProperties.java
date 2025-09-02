package com.alibaba.cloud.ai.a2a.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@ConfigurationProperties(prefix = "runtime")
public class RuntimeConfigProperties {

    private NewAgentConfigProperties.Model model;

    public NewAgentConfigProperties.Model getModel() {
        return model;
    }

    public void setModel(NewAgentConfigProperties.Model model) {
        this.model = model;
    }
}
