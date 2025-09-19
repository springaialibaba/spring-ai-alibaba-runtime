package runtime.engine.examples;

import runtime.engine.Runner;
import runtime.engine.agents.llm.LLMAgent;
import runtime.engine.agents.config.AgentConfig;
import runtime.engine.llms.QwenLLM;
import runtime.engine.memory.context.ContextManager;
import runtime.engine.schemas.agent.AgentRequest;
import runtime.engine.schemas.agent.Content;
import runtime.engine.schemas.agent.Event;
import runtime.engine.schemas.agent.Message;
import runtime.engine.schemas.agent.TextContent;
import runtime.engine.memory.model.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * 单轮对话LLM智能体示例
 */
public class SingleTurnLLMExample {

    public static void main(String[] args) {
        System.out.println("=== 单轮对话LLM智能体示例 ===");
        
        // 检查API密钥
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("请设置DASHSCOPE_API_KEY环境变量");
            return;
        }
        
        try {
            // 创建LLM和智能体
            QwenLLM model = new QwenLLM("qwen-turbo", apiKey);
            LLMAgent llmAgent = new LLMAgent(model, "single_turn_agent", "你是一个有用的助手", new AgentConfig());
            
            // 创建上下文管理器和运行器
            ContextManager contextManager = new ContextManager();
            
            try (Runner runner = new Runner(llmAgent, contextManager)) {
                System.out.println("智能体初始化完成");
                performSingleTurnConversation(runner);
            }
            
        } catch (Exception e) {
            System.err.println("运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void performSingleTurnConversation(Runner runner) {
        String question = "请给一个面包店起名字";
        System.out.println("用户问题: " + question);
        System.out.println("智能体正在处理您的请求...");

        try {
            AgentRequest request = createAgentRequest(question);
            StringBuilder response = new StringBuilder();

            runner.streamQuery(request)
                .doOnNext(event -> {
                    if (event instanceof Message) {
                        Message message = (Message) event;
                        if (MessageType.MESSAGE.name().equals(message.getType()) && 
                                "completed".equals(message.getStatus())) {
                            
                            if (message.getContent() != null && !message.getContent().isEmpty()) {
                                Content content = message.getContent().get(0);
                                if (content instanceof TextContent) {
                                    TextContent textContent = (TextContent) content;
                                    response.append(textContent.getText());
                                }
                            }
                        }
                    }
                })
                .blockLast(); // 等待完成

            System.out.println("智能体回复: " + response.toString());
            System.out.println("单轮对话完成！");
            
        } catch (Exception e) {
            System.err.println("对话出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static AgentRequest createAgentRequest(String question) {
        // 创建用户消息
        Message userMessage = new Message();
        userMessage.setRole("user");
        
        // 设置消息内容
        List<Content> content = new ArrayList<>();
        TextContent textContent = new TextContent();
        textContent.setText(question);
        content.add(textContent);
        userMessage.setContent(content);
        
        // 创建请求
        AgentRequest request = new AgentRequest();
        request.setInput(List.of(userMessage));
        
        return request;
    }
}
