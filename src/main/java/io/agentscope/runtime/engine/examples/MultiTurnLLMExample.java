package io.agentscope.runtime.engine.examples;

import io.agentscope.runtime.engine.Runner;
import io.agentscope.runtime.engine.agents.llm.LLMAgent;
import io.agentscope.runtime.engine.agents.config.AgentConfig;
import io.agentscope.runtime.engine.llms.QwenLLM;
import io.agentscope.runtime.engine.memory.context.ContextManager;
import io.agentscope.runtime.engine.memory.model.MessageType;
import io.agentscope.runtime.engine.schemas.agent.AgentRequest;
import io.agentscope.runtime.engine.schemas.agent.Content;
import io.agentscope.runtime.engine.schemas.agent.Event;
import io.agentscope.runtime.engine.schemas.agent.Message;
import io.agentscope.runtime.engine.schemas.agent.TextContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * 多轮对话LLM智能体示例
 */
public class MultiTurnLLMExample {

    private static final String CONVERSATION_ID = "multi_turn_conversation_001";
    private static final int MAX_TURNS = 10;

    public static void main(String[] args) {
        System.out.println("=== 多轮对话LLM智能体示例 ===");
        
        // 使用项目原本的配置系统
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("请设置DASHSCOPE_API_KEY环境变量");
            return;
        }
        
        QwenLLM model = new QwenLLM("qwen-turbo", apiKey);

        LLMAgent llmAgent = new LLMAgent(
            model,
            "multi_turn_agent",
            "多轮对话LLM智能体，能够维护对话上下文和历史",
            new AgentConfig()
        );

            System.out.println("LLM智能体创建成功");

            ContextManager contextManager = new ContextManager();
            
            try (Runner runner = new Runner(llmAgent, contextManager, "user_001", CONVERSATION_ID)) {
                System.out.println("智能体初始化完成");
                System.out.println("开始多轮对话！输入 'quit' 或 'exit' 退出");
                System.out.println("最大对话轮数: " + MAX_TURNS);
                System.out.println("----------------------------------------");
                
                performMultiTurnConversation(runner);
            } catch (Exception e) {
            System.err.println("运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void performMultiTurnConversation(Runner runner) {
        Scanner scanner = new Scanner(System.in);
        int turnCount = 0;

        while (turnCount < MAX_TURNS) {
            System.out.print("用户 (第" + (turnCount + 1) + "轮): ");
            String userInput = scanner.nextLine().trim();

            if ("quit".equalsIgnoreCase(userInput) || "exit".equalsIgnoreCase(userInput)) {
                System.out.println("用户选择退出对话");
                break;
            }

            if (userInput.isEmpty()) {
                System.out.println("请输入有效的问题或消息");
                continue;
            }

            try {
                AgentRequest request = createAgentRequest(userInput);
                
                System.out.println("智能体正在思考...");

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

                String agentResponse = response.toString();
                System.out.println("智能体: " + agentResponse);

                turnCount++;
                System.out.println("----------------------------------------");

            } catch (Exception e) {
                System.err.println("对话出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (turnCount >= MAX_TURNS) {
            System.out.println("已达到最大对话轮数限制");
        }
        
        System.out.println("多轮对话结束，共进行了 " + turnCount + " 轮对话");
        
        scanner.close();
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