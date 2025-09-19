/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.runtime.engine.infrastructure.config.memory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 记忆服务配置属性
 */
@ConfigurationProperties(prefix = "memory.service")
public class MemoryProperties {
    
    /**
     * 记忆服务类型：memory 或 redis 或 mysql
     */
    private String type = "memory";
    
    /**
     * 是否自动启动记忆服务
     */
    private boolean autoStart = true;
    
    /**
     * 是否在启动时执行健康检查
     */
    private boolean healthCheckOnStart = true;
    
    /**
     * 默认会话ID
     */
    private String defaultSessionId = "default_session";
    
    /**
     * 搜索时的默认top_k值
     */
    private int defaultTopK = 5;
    
    /**
     * 分页时的默认页面大小
     */
    private int defaultPageSize = 10;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    public boolean isHealthCheckOnStart() {
        return healthCheckOnStart;
    }
    
    public void setHealthCheckOnStart(boolean healthCheckOnStart) {
        this.healthCheckOnStart = healthCheckOnStart;
    }
    
    public String getDefaultSessionId() {
        return defaultSessionId;
    }
    
    public void setDefaultSessionId(String defaultSessionId) {
        this.defaultSessionId = defaultSessionId;
    }
    
    public int getDefaultTopK() {
        return defaultTopK;
    }
    
    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }
    
    public int getDefaultPageSize() {
        return defaultPageSize;
    }
    
    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
}
