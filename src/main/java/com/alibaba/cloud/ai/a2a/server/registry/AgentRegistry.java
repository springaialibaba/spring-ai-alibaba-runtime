package com.alibaba.cloud.ai.a2a.server.registry;

import com.alibaba.cloud.ai.a2a.server.config.AgentConfigProperties;
import io.a2a.spec.AgentCard;

import java.util.List;

/**
 *
 *
 * @author xiweng.yy
 */
public interface AgentRegistry {
    
    void register(AgentConfigProperties.AgentCardInfo agentCardInfo);
    
    /**
     * 注册自动生成的AgentCard
     */
    default void register(AgentCard agentCard) {
        // 默认实现：将AgentCard转换为AgentCardInfo
        AgentConfigProperties.AgentCardInfo agentCardInfo = convertToAgentCardInfo(agentCard);
        register(agentCardInfo);
    }
    
    /**
     * 将AgentCard转换为AgentCardInfo的默认实现
     */
    default AgentConfigProperties.AgentCardInfo convertToAgentCardInfo(AgentCard agentCard) {
        AgentConfigProperties.AgentCardInfo info = new AgentConfigProperties.AgentCardInfo();
        info.setName(agentCard.name());
        info.setDescription(agentCard.description());
        info.setUrl(agentCard.url());
        info.setVersion(agentCard.version());
        info.setDocumentationUrl(agentCard.documentationUrl());
        
        // 设置capabilities
        AgentConfigProperties.Capabilities capabilities = new AgentConfigProperties.Capabilities();
        capabilities.setStreaming(agentCard.capabilities().streaming());
        capabilities.setPushNotifications(agentCard.capabilities().pushNotifications());
        capabilities.setStateTransitionHistory(agentCard.capabilities().stateTransitionHistory());
        info.setCapabilities(capabilities);
        
        // 设置输入输出模式
        info.setDefaultInputModes(agentCard.defaultInputModes());
        info.setDefaultOutputModes(agentCard.defaultOutputModes());
        
        // 设置技能
        List<AgentConfigProperties.Skill> skills = agentCard.skills().stream()
                .map(skill -> {
                    AgentConfigProperties.Skill configSkill = new AgentConfigProperties.Skill();
                    configSkill.setId(skill.id());
                    configSkill.setName(skill.name());
                    configSkill.setDescription(skill.description());
                    configSkill.setTags(skill.tags());
                    configSkill.setExamples(skill.examples());
                    return configSkill;
                })
                .collect(java.util.stream.Collectors.toList());
        info.setSkills(skills);
        
        info.setProtocolVersion(agentCard.protocolVersion());
        
        return info;
    }
}
