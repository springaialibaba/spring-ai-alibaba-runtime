package runtime.domain.tools.service.fs;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;
import java.util.logging.Logger;

public class ReadMultipleFilesTool implements BiFunction<ReadMultipleFilesTool.Request, ToolContext, ReadMultipleFilesTool.Response> {

    Logger logger = Logger.getLogger(ReadMultipleFilesTool.class.getName());

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        try {
            BaseSandboxTools tools = new BaseSandboxTools();
            String result = tools.fs_read_multiple_files(request.paths);
            return new Response(result, "Filesystem read_multiple_files completed");
        } catch (Exception e) {
            return new Response("Error", "Filesystem read_multiple_files error: " + e.getMessage());
        }
    }

    public record Request(
            @JsonProperty(required = true, value = "paths")
            @JsonPropertyDescription("Paths to the files to read")
            String[] paths
    ) { }

    @JsonClassDescription("The result contains filesystem tool output and execution message")
    public record Response(String result, String message) {
        public Response(String result, String message) {
            this.result = result;
            this.message = message;
        }

        @JsonProperty(required = true, value = "result")
        @JsonPropertyDescription("tool output")
        public String result() { return this.result; }

        @JsonProperty(required = true, value = "message")
        @JsonPropertyDescription("execute result")
        public String message() { return this.message; }
    }
}


