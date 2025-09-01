package com.alibaba.cloud.ai.a2a.server.nacos.utils;

import com.alibaba.cloud.ai.a2a.server.config.AgentConfigProperties;
import com.alibaba.nacos.api.ai.model.a2a.AgentCapabilities;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentSkill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author xiweng.yy
 */
public class AgentCardUtil {
    
    public static AgentCard transferFromAgentCardInfo(AgentConfigProperties.AgentCardInfo agentCardInfo) {
        AgentCard agentCard = new AgentCard();
        agentCard.setProtocolVersion(agentCardInfo.getProtocolVersion());
        agentCard.setName(formatAgentName(agentCardInfo.getName()));
        agentCard.setDescription(agentCardInfo.getDescription());
        // TODO url can be auto register
        agentCard.setUrl(agentCardInfo.getUrl());
        agentCard.setVersion(agentCardInfo.getVersion());
        agentCard.setDocumentationUrl(agentCardInfo.getDocumentationUrl());
        agentCard.setDefaultInputModes(agentCardInfo.getDefaultInputModes());
        agentCard.setDefaultOutputModes(agentCardInfo.getDefaultOutputModes());
        agentCard.setProtocolVersion(agentCardInfo.getProtocolVersion());
        agentCard.setCapabilities(transferFromAgentCardInfoCapabilities(agentCardInfo.getCapabilities()));
        agentCard.setSkills(transferFromAgentCardInfoSkills(agentCardInfo.getSkills()));
        // TODO saa should support this auto.
        agentCard.setPreferredTransport("JSONRPC");
        // TODO add other fields.
        return agentCard;
    }
    
    public static AgentCapabilities transferFromAgentCardInfoCapabilities(
            AgentConfigProperties.Capabilities capabilities) {
        AgentCapabilities agentCapabilities = new AgentCapabilities();
        agentCapabilities.setStreaming(capabilities.isStreaming());
        agentCapabilities.setPushNotifications(capabilities.isPushNotifications());
        agentCapabilities.setStateTransitionHistory(capabilities.isStateTransitionHistory());
        // TODO saa should support this.
        agentCapabilities.setExtensions(Collections.emptyList());
        return agentCapabilities;
    }
    
    public static List<AgentSkill> transferFromAgentCardInfoSkills(List<AgentConfigProperties.Skill> skills) {
        List<AgentSkill> result = new ArrayList<>(skills.size());
        skills.forEach(skill -> {
            AgentSkill newSkill = new AgentSkill();
            newSkill.setId(skill.getId());
            newSkill.setName(skill.getName());
            newSkill.setDescription(skill.getDescription());
            newSkill.setTags(skill.getTags());
            newSkill.setExamples(skill.getExamples());
            // TODO saa should support this.
            newSkill.setInputModes(Collections.emptyList());
            newSkill.setOutputModes(Collections.emptyList());
            result.add(newSkill);
        });
        return result;
    }
    
    private static String formatAgentName(String originalAgentName) {
        return originalAgentName.replaceAll("\\s", "_");
    }
}
