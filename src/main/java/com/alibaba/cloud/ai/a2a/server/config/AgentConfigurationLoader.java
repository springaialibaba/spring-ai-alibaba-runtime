package com.alibaba.cloud.ai.a2a.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(AgentConfigProperties.class)
public class AgentConfigurationLoader {

	private static final Logger logger = LoggerFactory.getLogger(AgentConfigurationLoader.class);

	private final AgentConfigProperties config;

	public AgentConfigurationLoader(AgentConfigProperties config) {
		this.config = config;
	}

	@PostConstruct
	public void validateConfiguration() {
		logger.info("正在验证代理配置...");

		// 验证DashScope配置
		if (config.getDashScope() == null || config.getDashScope().getApiKey() == null) {
			logger.warn("DashScope API密钥未配置，将使用环境变量");
		}

		// 验证代理配置
		if (config.getAgents() == null || config.getAgents().isEmpty()) {
			throw new IllegalStateException("代理配置不能为空");
		}

		// 验证根代理配置
		if (config.getRootAgent() == null) {
			throw new IllegalStateException("根代理配置不能为空");
		}

		// 验证代理卡片配置
		if (config.getAgentCard() == null) {
			throw new IllegalStateException("代理卡片配置不能为空");
		}

		logger.info("代理配置验证完成");
		logger.info("已配置 {} 个代理", config.getAgents().size());
		logger.info("根代理: {}", config.getRootAgent().getName());
	}

}
