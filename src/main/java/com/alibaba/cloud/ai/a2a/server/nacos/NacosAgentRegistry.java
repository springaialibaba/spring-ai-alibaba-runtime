package com.alibaba.cloud.ai.a2a.server.nacos;

import com.alibaba.cloud.ai.a2a.server.config.NewAgentConfigProperties;
import com.alibaba.cloud.ai.a2a.server.nacos.config.NacosConfigProperties;
import com.alibaba.cloud.ai.a2a.server.nacos.utils.AgentCardUtil;
import com.alibaba.cloud.ai.a2a.server.registry.AgentRegistry;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.maintainer.client.ai.A2aMaintainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author xiweng.yy
 */
public class NacosAgentRegistry implements AgentRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosAgentRegistry.class);
    
    private final A2aMaintainerService a2aMaintainerService;
    
    private final NacosConfigProperties nacosConfigProperties;
    
    public NacosAgentRegistry(A2aMaintainerService a2aMaintainerService, NacosConfigProperties nacosConfigProperties) {
        this.a2aMaintainerService = a2aMaintainerService;
        this.nacosConfigProperties = nacosConfigProperties;
    }
    
    private AgentCard buildAgentCardFromRootAgent(NewAgentConfigProperties.AgentCardInfo agentCardInfo) {
        return AgentCardUtil.transferFromAgentCardInfo(agentCardInfo);
    }
    
    @Override
    public void register(NewAgentConfigProperties.AgentCardInfo agentCardInfo) {
        LOGGER.info("自动注册Agent{}到Nacos中", agentCardInfo.getName());
        try {
            AgentCard agentCard = buildAgentCardFromRootAgent(agentCardInfo);
            a2aMaintainerService.registerAgent(agentCard, nacosConfigProperties.getNamespace());
            LOGGER.info("自动注册Agent{}到Nacos成功", agentCard.getName());
        } catch (NacosException e) {
            LOGGER.warn("Auto Register Agent {} failed", agentCardInfo.getName(), e);
        }
    }
}
