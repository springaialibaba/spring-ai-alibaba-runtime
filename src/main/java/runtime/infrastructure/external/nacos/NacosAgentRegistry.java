package runtime.infrastructure.external.nacos;

import runtime.infrastructure.config.agent.AgentProperties;
import runtime.infrastructure.config.nacos.NacosProperties;
import runtime.infrastructure.external.nacos.util.AgentCardUtil;
import runtime.infrastructure.external.registry.AgentRegistry;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author xiweng.yy
 */
public class NacosAgentRegistry implements AgentRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosAgentRegistry.class);
    
    private final AiMaintainerService aiMaintainerService;
    
    private final NacosProperties nacosProperties;
    
    public NacosAgentRegistry(AiMaintainerService aiMaintainerService, NacosProperties nacosProperties) {
        this.aiMaintainerService = aiMaintainerService;
        this.nacosProperties = nacosProperties;
    }
    
    private AgentCard buildAgentCardFromRootAgent(AgentProperties.AgentCardInfo agentCardInfo) {
        return AgentCardUtil.transferFromAgentCardInfo(agentCardInfo);
    }
    
    @Override
    public void register(AgentProperties.AgentCardInfo agentCardInfo) {
        LOGGER.info("自动注册Agent{}到Nacos中", agentCardInfo.getName());
        try {
            AgentCard agentCard = buildAgentCardFromRootAgent(agentCardInfo);
            aiMaintainerService.registerAgent(agentCard, nacosProperties.getNamespace());
            LOGGER.info("自动注册Agent{}到Nacos成功", agentCard.getName());
        } catch (NacosException e) {
            LOGGER.warn("Auto Register Agent {} failed", agentCardInfo.getName(), e);
        }
    }
}
