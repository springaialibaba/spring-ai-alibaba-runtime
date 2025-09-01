package com.alibaba.cloud.ai.a2a.server;

import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.a2a.A2A;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;

@Service
public class GraphAgentExecutor implements AgentExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphAgentExecutor.class);

    private final BaseAgent ExecuteAgent;

    public GraphAgentExecutor(BaseAgent ExecuteAgent) {
        this.ExecuteAgent = ExecuteAgent;
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
            StringBuilder sb = new StringBuilder();
            for (Part<?> each : message.getParts()) {
                if (Part.Kind.TEXT.equals(each.getKind())) {
                    sb.append(((TextPart) each).getText()).append("\n");
                }
            }
            Map<String, Object> input = Map.of("input", sb.toString().trim());

            AsyncGenerator<NodeOutput> resultFuture = ExecuteAgent.stream(input);

            Task task = context.getTask();
            if (task == null) {
                task = new_task(context.getMessage());
                eventQueue.enqueueEvent(task);
            }
            TaskUpdater taskUpdater = new TaskUpdater(context, eventQueue);

            try {
                processStreamingOutput(resultFuture, taskUpdater);
            } catch (Exception e) {
                LOGGER.error("Error processing streaming output", e);
                taskUpdater.startWork(taskUpdater.newAgentMessage(
                        List.of(new TextPart("Error processing streaming output: " + e.getMessage())),
                        Map.of()
                ));
                taskUpdater.complete();
            }

        } catch (Exception e) {
            LOGGER.error("Agent execution failed", e);
            eventQueue.enqueueEvent(A2A.toAgentMessage("Agent execution failed: " + e.getMessage()));
        }
    }

    /**
     * 处理流式输出数据
     */
    private void processStreamingOutput(AsyncGenerator<NodeOutput> streamGenerator, TaskUpdater taskUpdater) {
        try {
            StringBuilder accumulatedOutput = new StringBuilder();

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

    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
    }
}
