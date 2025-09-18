package runtime.engine.examples;

import reactor.core.publisher.Flux;
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * LLM智能体交互示例
 */
public class LLMAgentExample {

    public static void main(String[] args) {
        if (args.length > 0 && "testMultipleQuestions".equals(args[0])) {
            testMultipleQuestions();
        } else {
            System.out.println("开始创建LLM智能体...");

            QwenLLM model = new QwenLLM(
                "qwen-turbo",
                System.getenv("DASHSCOPE_API_KEY")
            );

            LLMAgent llmAgent = new LLMAgent(
                model,
                "llm_agent",
                "A simple LLM agent for text generation",
                new AgentConfig()
            );

            System.out.println("LLM智能体创建成功");

            ContextManager contextManager = new ContextManager();

            try (Runner runner = new Runner(llmAgent, contextManager)) {
                System.out.println("Runner创建成功");
                interactWithAgent(runner);
            } catch (Exception e) {
                System.err.println("运行出错: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("程序执行完毕！");
        }
    }

    /**
     * 测试多个问题
     */
    public static void testMultipleQuestions() {
        System.out.println("开始多问题测试...");

        QwenLLM model = new QwenLLM("qwen-turbo", "test_api_key");

        LLMAgent llmAgent = new LLMAgent(
            model,
            "test_agent",
            "Test LLM agent for multiple questions",
            new AgentConfig()
        );

        ContextManager contextManager = new ContextManager();

        String[] questions = {
            "法国的首都是什么？",
            "你好",
            "什么是人工智能？",
            "hello"
        };

        try (Runner runner = new Runner(llmAgent, contextManager)) {
            for (String question : questions) {
                System.out.println("\n问题: " + question);

                AgentRequest request = createAgentRequest(question);

                List<Event> events = runner.streamQuery(request)
                    .collectList()
                    .block();

                String response = extractResponse(events);
                System.out.println("回答: " + response);
            }
        } catch (Exception e) {
            System.err.println("测试出错: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n多问题测试完成！");
    }

    /**
     * 从事件列表中提取响应文本
     */
    private static String extractResponse(List<Event> events) {
        StringBuilder response = new StringBuilder();

        for (Event event : events) {
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
        }

        return response.toString();
    }

    /**
     * 与智能体交互
     */
    private static void interactWithAgent(Runner runner) {
        AgentRequest request = createAgentRequest();

        System.out.println("智能体正在处理您的请求...");

        StringBuilder allResult = new StringBuilder();

        try {
            List<Event> events = runner.streamQuery(request)
                .doOnNext(event -> {
                    if (event instanceof Message) {
                        Message message = (Message) event;
                        if (MessageType.MESSAGE.name().equals(message.getType()) && 
                            "completed".equals(message.getStatus())) {

                            if (message.getContent() != null && !message.getContent().isEmpty()) {
                                Content content = message.getContent().get(0);
                                if (content instanceof TextContent) {
                                    TextContent textContent = (TextContent) content;
                                    allResult.append(textContent.getText());
                                }
                            }
                        }
                    }
                })
                .collectList()
                .block();

            System.out.println("智能体回复: " + allResult.toString());
            System.out.println("交互完成！");

        } catch (Exception e) {
            System.err.println("交互出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建Agent请求
     */
    private static AgentRequest createAgentRequest() {
        return createAgentRequest("法国的首都是什么？");
    }

    /**
     * 创建指定问题的Agent请求
     */
    private static AgentRequest createAgentRequest(String question) {
        Message userMessage = new Message();
        userMessage.setRole("user");

        List<Content> content = new ArrayList<>();
        TextContent textContent = new TextContent();
        textContent.setText(question);
        content.add(textContent);

        userMessage.setContent(content);

        AgentRequest request = new AgentRequest();
        request.setInput(List.of(userMessage));

        return request;
    }

    /**
     * 异步交互示例
     */
    public static void asyncInteractWithAgent() {
        System.out.println("开始异步交互...");

        QwenLLM model = new QwenLLM(
            "qwen-turbo",
            System.getenv("DASHSCOPE_API_KEY")
        );

        LLMAgent llmAgent = new LLMAgent(model);

        ContextManager contextManager = new ContextManager();

        CompletableFuture.runAsync(() -> {
            try (Runner runner = new Runner(llmAgent, contextManager)) {
                System.out.println("异步Runner创建成功");

                AgentRequest request = createAgentRequest();

                System.out.println("异步智能体正在处理您的请求...");

                runner.streamQuery(request)
                    .subscribe(
                        event -> {
                            if (event instanceof Message) {
                                Message message = (Message) event;
                                if (MessageType.MESSAGE.name().equals(message.getType())) {
                                    System.out.print("流式响应: ");
                                    if (message.getContent() != null && !message.getContent().isEmpty()) {
                                        Content content = message.getContent().get(0);
                                        if (content instanceof TextContent) {
                                            TextContent textContent = (TextContent) content;
                                            System.out.print(textContent.getText());
                                        }
                                    }
                                    System.out.println();
                                }
                            }
                        },
                        error -> System.err.println("异步交互出错: " + error.getMessage()),
                        () -> System.out.println("异步交互完成")
                    );

            } catch (Exception e) {
                System.err.println("异步运行出错: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}