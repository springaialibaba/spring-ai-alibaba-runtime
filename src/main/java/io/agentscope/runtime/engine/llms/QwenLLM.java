package io.agentscope.runtime.engine.llms;

import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 通义千问LLM实现
 */
public class QwenLLM implements BaseLLM {

    private static final Logger LOGGER = LoggerFactory.getLogger(QwenLLM.class);

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final ChatModel chatModel;

    public QwenLLM(String modelName, String apiKey) {
        this(modelName, apiKey, "https://dashscope.aliyuncs.com/api/v1");
    }

    public QwenLLM(String modelName, String apiKey, String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        
        // 创建DashScope ChatModel
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(modelName)
                .withTemperature(0.7)
                .withMaxToken(2000)
                .build();
        
        this.chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
    }

    @Override
    public Flux<ChatChunk> chatStream(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        return Flux.create(sink -> {
            try {
                // 使用非流式调用获取完整响应，然后模拟流式输出
                String response = chat(messages, tools).getContent();
                LOGGER.info("开始流式输出，总长度: {} 字符", response.length());
                
                // 按字符逐个输出，模拟真实的流式体验
                for (int i = 0; i < response.length(); i++) {
                    String character = String.valueOf(response.charAt(i));
                    
                    ChatChunk.Delta delta = new ChatChunk.Delta(character);
                    ChatChunk.Choice choice = new ChatChunk.Choice(delta);
                    ChatChunk chunk = new ChatChunk(List.of(choice));
                    sink.next(chunk);
                    
                    // 模拟网络延迟，让用户看到思考过程
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        sink.error(e);
                        return;
                    }
                }
                
                LOGGER.info("流式输出完成");
                sink.complete();
            } catch (Exception e) {
                LOGGER.error("Error in chatStream", e);
                sink.error(e);
            }
        });
    }

    @Override
    public ChatResponse chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        try {
            List<Message> springAiMessages = convertToSpringAiMessages(messages);
            Prompt prompt = new Prompt(springAiMessages);
            
            LOGGER.info("调用通义千问API，消息数量: {}", springAiMessages.size());
            for (int i = 0; i < springAiMessages.size(); i++) {
                Message msg = springAiMessages.get(i);
                LOGGER.info("消息 {}: [{}]", i+1, msg.getClass().getSimpleName());
            }
            
            org.springframework.ai.chat.model.ChatResponse springResponse = chatModel.call(prompt);
            // 使用 toString() 方法暂时获取内容，稍后修复正确的API
            String content = springResponse.toString();
            if (content.contains("content=")) {
                int start = content.indexOf("content=") + 8;
                int end = content.indexOf(",", start);
                if (end == -1) end = content.indexOf("}", start);
                if (end == -1) end = content.length();
                content = content.substring(start, end).trim();
                if (content.startsWith("\"") && content.endsWith("\"")) {
                    content = content.substring(1, content.length() - 1);
                }
            }
            
            LOGGER.info("通义千问API响应长度: {} 字符", content.length());
            
            Map<String, Object> usage = new HashMap<>();
            usage.put("prompt_tokens", 100);
            usage.put("completion_tokens", content.length() / 4);
            usage.put("total_tokens", 100 + content.length() / 4);

            return new ChatResponse(content, usage);
        } catch (Exception e) {
            LOGGER.error("调用通义千问API失败", e);
            throw new RuntimeException("Chat failed", e);
        }
    }

    private List<Message> convertToSpringAiMessages(List<Map<String, Object>> messages) {
        List<Message> springAiMessages = new ArrayList<>();
        
        for (Map<String, Object> message : messages) {
            String role = (String) message.get("role");
            List<Map<String, Object>> content = (List<Map<String, Object>>) message.get("content");
            
            if (content != null && !content.isEmpty()) {
                StringBuilder textBuilder = new StringBuilder();
                for (Map<String, Object> contentItem : content) {
                    if ("text".equals(contentItem.get("type"))) {
                        textBuilder.append((String) contentItem.get("text"));
                    }
                }
                
                String text = textBuilder.toString();
                if (!text.isEmpty()) {
                    switch (role) {
                        case "system":
                            springAiMessages.add(new SystemMessage(text));
                            break;
                        case "user":
                            springAiMessages.add(new UserMessage(text));
                            break;
                        case "assistant":
                            springAiMessages.add(new AssistantMessage(text));
                            break;
                        default:
                            springAiMessages.add(new UserMessage(text));
                            break;
                    }
                }
            }
        }
        
        return springAiMessages;
    }
}