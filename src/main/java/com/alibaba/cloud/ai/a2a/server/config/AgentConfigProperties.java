package com.alibaba.cloud.ai.a2a.server.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.agent")
public class AgentConfigProperties {

    private DashScope dashScope;

    private Map<String, KeyStrategyConfig> keyStrategies;

    private List<AgentDefinition> agents;

    private RootAgent rootAgent;

    private AgentCardInfo agentCard;

    private AgentRegistryInfo registry;

    public static class DashScope {

        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

    }

    public static class KeyStrategyConfig {

        private String type;

        private String description;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    public static class AgentDefinition {

        private String name;

        private String description;

        private String instruction;

        private String outputKey;

        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }

        public String getOutputKey() {
            return outputKey;
        }

        public void setOutputKey(String outputKey) {
            this.outputKey = outputKey;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public static class RootAgent {

        private String name;

        private String type; // 可选：缺省为 LlmRoutingAgent

        private String inputKey;

        private String outputKey;

        private List<String> subAgentNames;

        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getInputKey() {
            return inputKey;
        }

        public void setInputKey(String inputKey) {
            this.inputKey = inputKey;
        }

        public String getOutputKey() {
            return outputKey;
        }

        public void setOutputKey(String outputKey) {
            this.outputKey = outputKey;
        }

        public List<String> getSubAgentNames() {
            return subAgentNames;
        }

        public void setSubAgentNames(List<String> subAgentNames) {
            this.subAgentNames = subAgentNames;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    public static class AgentCardInfo {

        private String name;

        private String description;

        private String url;

        private String version;

        private String documentationUrl;

        private Capabilities capabilities;

        private List<String> defaultInputModes;

        private List<String> defaultOutputModes;

        private List<Skill> skills;

        private String protocolVersion;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDocumentationUrl() {
            return documentationUrl;
        }

        public void setDocumentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
        }

        public Capabilities getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(Capabilities capabilities) {
            this.capabilities = capabilities;
        }

        public List<String> getDefaultInputModes() {
            return defaultInputModes;
        }

        public void setDefaultInputModes(List<String> defaultInputModes) {
            this.defaultInputModes = defaultInputModes;
        }

        public List<String> getDefaultOutputModes() {
            return defaultOutputModes;
        }

        public void setDefaultOutputModes(List<String> defaultOutputModes) {
            this.defaultOutputModes = defaultOutputModes;
        }

        public List<Skill> getSkills() {
            return skills;
        }

        public void setSkills(List<Skill> skills) {
            this.skills = skills;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

    }

    public static class Capabilities {

        private boolean streaming;

        private boolean pushNotifications;

        private boolean stateTransitionHistory;

        public boolean isStreaming() {
            return streaming;
        }

        public void setStreaming(boolean streaming) {
            this.streaming = streaming;
        }

        public boolean isPushNotifications() {
            return pushNotifications;
        }

        public void setPushNotifications(boolean pushNotifications) {
            this.pushNotifications = pushNotifications;
        }

        public boolean isStateTransitionHistory() {
            return stateTransitionHistory;
        }

        public void setStateTransitionHistory(boolean stateTransitionHistory) {
            this.stateTransitionHistory = stateTransitionHistory;
        }

    }

    public static class Skill {

        private String id;

        private String name;

        private String description;

        private List<String> tags;

        private List<String> examples;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getExamples() {
            return examples;
        }

        public void setExamples(List<String> examples) {
            this.examples = examples;
        }

    }

    public static class AgentRegistryInfo {

        private String type;

        private boolean enabled = true;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    // Getters and setters for main class
    public DashScope getDashScope() {
        return dashScope;
    }

    public void setDashScope(DashScope dashScope) {
        this.dashScope = dashScope;
    }

    public Map<String, KeyStrategyConfig> getKeyStrategies() {
        return keyStrategies;
    }

    public void setKeyStrategies(Map<String, KeyStrategyConfig> keyStrategies) {
        this.keyStrategies = keyStrategies;
    }

    public List<AgentDefinition> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentDefinition> agents) {
        this.agents = agents;
    }

    public RootAgent getRootAgent() {
        return rootAgent;
    }

    public void setRootAgent(RootAgent rootAgent) {
        this.rootAgent = rootAgent;
    }

    public AgentCardInfo getAgentCard() {
        return agentCard;
    }

    public void setAgentCard(AgentCardInfo agentCard) {
        this.agentCard = agentCard;
    }

    public AgentRegistryInfo getRegistry() {
        return registry;
    }

    public void setRegistry(AgentRegistryInfo registry) {
        this.registry = registry;
    }
}
