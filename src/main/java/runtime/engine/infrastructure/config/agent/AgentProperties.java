package runtime.engine.infrastructure.config.agent;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@ConfigurationProperties(prefix = "")
public class AgentProperties {

    private List<AgentDefinition> agents;

    private AgentRegistryInfo registry;

    public static class AgentDefinition {

        private String name;

        private String description;

        private String instruction;

        private String outputKey;

        private String type;

        @JsonProperty("input_key")
        private String inputKey;

        @JsonProperty("isRoot")
        private Boolean isRoot;

        @JsonProperty("max_iterations")
        private Integer maxIterations;

        private List<String> tools;

        private String resolver;

        @JsonProperty("chat_options")
        private Map<String, Object> chatOptions;

        @JsonProperty("compile_config")
        private Map<String, Object> compileConfig;

        private State state;

        private Hooks hooks;

        private List<String> subAgentNames;

        private Model model;

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

        public String getInputKey() {
            return inputKey;
        }

        public void setInputKey(String inputKey) {
            this.inputKey = inputKey;
        }

        public Boolean getIsRoot() {
            return isRoot;
        }

        public void setIsRoot(Boolean isRoot) {
            this.isRoot = isRoot;
        }

        public Integer getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(Integer maxIterations) {
            this.maxIterations = maxIterations;
        }

        public List<String> getTools() {
            return tools;
        }

        public void setTools(List<String> tools) {
            this.tools = tools;
        }

        public String getResolver() {
            return resolver;
        }

        public void setResolver(String resolver) {
            this.resolver = resolver;
        }

        public Map<String, Object> getChatOptions() {
            return chatOptions;
        }

        public void setChatOptions(Map<String, Object> chatOptions) {
            this.chatOptions = chatOptions;
        }

        public Map<String, Object> getCompileConfig() {
            return compileConfig;
        }

        public void setCompileConfig(Map<String, Object> compileConfig) {
            this.compileConfig = compileConfig;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public Hooks getHooks() {
            return hooks;
        }

        public void setHooks(Hooks hooks) {
            this.hooks = hooks;
        }

        public List<String> getSubAgentNames() {
            return subAgentNames;
        }

        public void setSubAgentNames(List<String> subAgentNames) {
            this.subAgentNames = subAgentNames;
        }

        public Model getModel() {
            return model;
        }

        public void setModel(Model model) {
            this.model = model;
        }
    }

    public static class Model {
        private String provider;
        private String name;
        private Map<String, Object> options;
        @JsonProperty("chat_client_bean")
        private String chatClientBean;

        private String apiKey;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Object> options) {
            this.options = options;
        }

        public String getChatClientBean() {
            return chatClientBean;
        }

        public void setChatClientBean(String chatClientBean) {
            this.chatClientBean = chatClientBean;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class State {
        private Map<String, String> strategies;

        public Map<String, String> getStrategies() {
            return strategies;
        }

        public void setStrategies(Map<String, String> strategies) {
            this.strategies = strategies;
        }
    }

    public static class Hooks {
        @JsonProperty("pre_llm")
        private List<String> preLlm;
        @JsonProperty("post_llm")
        private List<String> postLlm;
        @JsonProperty("pre_tool")
        private List<String> preTool;
        @JsonProperty("post_tool")
        private List<String> postTool;

        public List<String> getPreLlm() {
            return preLlm;
        }

        public void setPreLlm(List<String> preLlm) {
            this.preLlm = preLlm;
        }

        public List<String> getPostLlm() {
            return postLlm;
        }

        public void setPostLlm(List<String> postLlm) {
            this.postLlm = postLlm;
        }

        public List<String> getPreTool() {
            return preTool;
        }

        public void setPreTool(List<String> preTool) {
            this.preTool = preTool;
        }

        public List<String> getPostTool() {
            return postTool;
        }

        public void setPostTool(List<String> postTool) {
            this.postTool = postTool;
        }
    }

    public static class AgentRegistryInfo {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public List<AgentDefinition> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentDefinition> agents) {
        this.agents = agents;
    }

    public AgentRegistryInfo getRegistry() {
        return registry;
    }

    public void setRegistry(AgentRegistryInfo registry) {
        this.registry = registry;
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
}
