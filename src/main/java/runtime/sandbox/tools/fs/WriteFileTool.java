package runtime.sandbox.tools.fs;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;
import java.util.logging.Logger;

public class WriteFileTool implements BiFunction<WriteFileTool.Request, ToolContext, WriteFileTool.Response> {

    Logger logger = Logger.getLogger(WriteFileTool.class.getName());

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        try {
            SandboxTools tools = new SandboxTools();
            String result = tools.fs_write_file(request.path, request.content);
            return new Response(result, "Filesystem write_file completed");
        } catch (Exception e) {
            return new Response("Error", "Filesystem write_file error: " + e.getMessage());
        }
    }

    public record Request(
            @JsonProperty(required = true, value = "path")
            @JsonPropertyDescription("Path to the file to write to")
            String path,
            @JsonProperty(required = true, value = "content")
            @JsonPropertyDescription("Content to write into the file")
            String content
    ) { }

    @JsonClassDescription("The result contains filesystem tool output and execution message")
    public record Response(String result, String message) {
        public Response(String result, String message) { this.result = result; this.message = message; }
        @JsonProperty(required = true, value = "result")
        @JsonPropertyDescription("tool output")
        public String result() { return this.result; }
        @JsonProperty(required = true, value = "message")
        @JsonPropertyDescription("execute result")
        public String message() { return this.message; }
    }
}


