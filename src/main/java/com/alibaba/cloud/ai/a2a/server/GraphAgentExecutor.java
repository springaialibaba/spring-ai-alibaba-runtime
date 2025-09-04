package com.alibaba.cloud.ai.a2a.server;

import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.cloud.ai.a2a.server.memory.MemoryService;
import com.alibaba.cloud.ai.a2a.server.memory.SessionHistoryService;
import com.alibaba.cloud.ai.a2a.server.memory.Session;
import com.alibaba.cloud.ai.a2a.server.memory.MessageType;
import com.alibaba.cloud.ai.a2a.server.memory.MessageContent;
import io.a2a.A2A;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GraphAgentExecutor implements AgentExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphAgentExecutor.class);

    private final BaseAgent ExecuteAgent;
    private final MemoryService memoryService;
    private final SessionHistoryService sessionHistoryService;

    public GraphAgentExecutor(BaseAgent ExecuteAgent, MemoryService memoryService, SessionHistoryService sessionHistoryService) {
        this.ExecuteAgent = ExecuteAgent;
        this.memoryService = memoryService;
        this.sessionHistoryService = sessionHistoryService;
    }

    private Task new_task(Message request) {
        String context_id_str = request.getContextId();
        if (context_id_str == null || context_id_str.isEmpty()) {
            context_id_str = java.util.UUID.randomUUID().toString();
        }
        String id = java.util.UUID.randomUUID().toString();
        if (request.getTaskId() != null && !request.getTaskId().isEmpty()) {
            id = request.getTaskId();
        }
        return new Task(id, context_id_str, new TaskStatus(TaskState.SUBMITTED), null, List.of(request), null);
    }

    @Override
    public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        try {
            Message message = context.getParams().message();
            // 准备/创建会话
            String contextId = message.getContextId() == null || message.getContextId().isEmpty()
                    ? java.util.UUID.randomUUID().toString()
                    : message.getContextId();
            Session session = sessionHistoryService.getSession(contextId, contextId).join().orElse(sessionHistoryService.createSession(contextId, java.util.Optional.of(contextId)).join());

            // 记录用户输入到会话与长期记忆
            com.alibaba.cloud.ai.a2a.server.memory.Message userMsg = buildTextMessage(getTextFromMessageParts(message));
            sessionHistoryService.appendMessage(session, java.util.List.of(userMsg));
            memoryService.addMemory(contextId, java.util.List.of(userMsg), java.util.Optional.of(contextId));

            String inputText = getTextFromMessageParts(message);
            // 检索相关记忆并拼接到输入前缀
            java.util.List<com.alibaba.cloud.ai.a2a.server.memory.Message> retrieved = memoryService.searchMemory(
                    contextId,
                    java.util.List.of(userMsg),
                    java.util.Optional.of(java.util.Map.of("top_k", 5))
            ).join();
            String retrievedText = formatRetrievedText(retrieved);
            String finalInput = retrievedText.isEmpty() ? inputText : (retrievedText + "\n\n" + inputText);
            Map<String, Object> input = Map.of("input", finalInput);

            AsyncGenerator<NodeOutput> resultFuture = ExecuteAgent.stream(input);

            Task task = context.getTask();
            if (task == null) {
                task = new_task(context.getMessage());
                eventQueue.enqueueEvent(task);
            }
            TaskUpdater taskUpdater = new TaskUpdater(context, eventQueue);

            StringBuilder accumulatedOutput = new StringBuilder();
            try {
                processStreamingOutput(resultFuture, taskUpdater, accumulatedOutput);
            } catch (Exception e) {
                LOGGER.error("Error processing streaming output", e);
                taskUpdater.startWork(taskUpdater.newAgentMessage(
                        List.of(new TextPart("Error processing streaming output: " + e.getMessage())),
                        Map.of()
                ));
                taskUpdater.complete();
            }

            // 将最终助手输出写入会话与长期记忆
            if (accumulatedOutput.length() > 0) {
                com.alibaba.cloud.ai.a2a.server.memory.Message assistantMsg = buildTextMessage(accumulatedOutput.toString());
                sessionHistoryService.appendMessage(session, java.util.List.of(assistantMsg));
                memoryService.addMemory(contextId, java.util.List.of(assistantMsg), java.util.Optional.of(contextId));
            }

        } catch (Exception e) {
            LOGGER.error("Agent execution failed", e);
            eventQueue.enqueueEvent(A2A.toAgentMessage("Agent execution failed: " + e.getMessage()));
        }
    }

    /**
     * 处理流式输出数据
     */
    private void processStreamingOutput(AsyncGenerator<NodeOutput> streamGenerator, TaskUpdater taskUpdater, StringBuilder accumulatedOutput) {
        try {

            streamGenerator.forEachAsync(output -> {
                try {
                    LOGGER.info("处理output: {}", output);

                    String content;
                    if (output instanceof StreamingOutput streamingOutput) {
                        content = streamingOutput.chunk();
                        if (content != null && !content.isEmpty()) {
                            taskUpdater.startWork(taskUpdater.newAgentMessage(
                                    List.of(new TextPart(content)),
                                    Map.of()
                            ));
                            accumulatedOutput.append(content);
                        }
                    } else {
                        Map<String, Object> stateData = output.state().data();
                        if (stateData.containsKey("output")) {
                            content = String.valueOf(stateData.get("output"));
                            if (content != null && !content.isEmpty()) {
                                taskUpdater.startWork(taskUpdater.newAgentMessage(
                                        List.of(new TextPart(content)),
                                        Map.of()
                                ));
                                accumulatedOutput.append(content);
                            }
                        } else {
                            LOGGER.debug("NodeOutput中没有找到output字段，可用字段: {}", stateData.keySet());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("处理单个output时发生错误", e);
                    // 继续处理下一个，不要因为单个错误就终止
                }
            }).thenAccept(v -> taskUpdater.complete()).exceptionally(e -> {
                LOGGER.error("流式处理发生错误", e);
                taskUpdater.startWork(taskUpdater.newAgentMessage(
                        List.of(new TextPart("Streaming error occurred: " + e.getMessage())),
                        Map.of()
                ));
                taskUpdater.complete();
                return null;
            });

        } catch (Exception e) {
            LOGGER.error("Error in processStreamingOutput", e);
            taskUpdater.startWork(taskUpdater.newAgentMessage(
                    List.of(new TextPart("Error processing streaming output: " + e.getMessage())),
                    Map.of()
            ));
            taskUpdater.complete();
        }
    }

    private static String getTextFromMessageParts(Message message) {
        StringBuilder sb = new StringBuilder();
        for (Part<?> each : message.getParts()) {
            if (Part.Kind.TEXT.equals(each.getKind())) {
                sb.append(((TextPart) each).getText()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static com.alibaba.cloud.ai.a2a.server.memory.Message buildTextMessage(String text) {
        MessageContent content = new MessageContent("text", text);
        return new com.alibaba.cloud.ai.a2a.server.memory.Message(MessageType.MESSAGE, java.util.List.of(content));
    }

    private static String formatRetrievedText(java.util.List<com.alibaba.cloud.ai.a2a.server.memory.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[Retrieved Memory]\n");
        int idx = 1;
        for (com.alibaba.cloud.ai.a2a.server.memory.Message m : messages) {
            String text = extractTextFromMessage(m);
            if (!text.isEmpty()) {
                sb.append(idx++).append('.').append(' ').append(text).append('\n');
            }
        }
        return sb.toString().trim();
    }

    private static String extractTextFromMessage(com.alibaba.cloud.ai.a2a.server.memory.Message message) {
        if (message == null || message.getContent() == null) {
            return "";
        }
        for (MessageContent content : message.getContent()) {
            if ("text".equals(content.getType())) {
                return content.getText();
            }
        }
        return "";
    }

    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
    }
}
