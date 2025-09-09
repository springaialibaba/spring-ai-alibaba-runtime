package runtime.domain.tools.service.fs;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;
import java.util.logging.Logger;

public class GetFileInfoTool implements BiFunction<GetFileInfoTool.Request, ToolContext, GetFileInfoTool.Response> {

    Logger logger = Logger.getLogger(GetFileInfoTool.class.getName());

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        try {
            SandboxTools tools = new SandboxTools();
            String result = tools.fs_get_file_info(request.path);
            return new Response(result, "Filesystem get_file_info completed");
        } catch (Exception e) {
            return new Response("Error", "Filesystem get_file_info error: " + e.getMessage());
        }
    }

    public record Request(
            @JsonProperty(required = true, value = "path")
            @JsonPropertyDescription("Path to the file or directory")
            String path
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


