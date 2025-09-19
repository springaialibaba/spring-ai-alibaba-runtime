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
package io.agentscope.runtime.engine.infrastructure.config.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(AgentProperties.class)
public class AgentConfigurationLoader {

	private static final Logger logger = LoggerFactory.getLogger(AgentConfigurationLoader.class);

	private final AgentProperties config;

	public AgentConfigurationLoader(AgentProperties config) {
		this.config = config;
	}

	@PostConstruct
	public void validateConfiguration() {
		logger.info("正在验证代理配置...");

		// 验证代理配置
		if (config.getAgents() == null || config.getAgents().isEmpty()) {
			throw new IllegalStateException("代理配置不能为空");
		}

		// AgentCard现在是自动生成的，无需验证手动配置
		logger.info("代理配置验证完成");
		logger.info("已配置 {} 个代理", config.getAgents().size());
		logger.info("AgentCard将自动生成");
	}

}
