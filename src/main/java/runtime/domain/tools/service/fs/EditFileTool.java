package runtime.domain.tools.service.fs;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class EditFileTool implements BiFunction<EditFileTool.Request, ToolContext, EditFileTool.Response> {

    Logger logger = Logger.getLogger(EditFileTool.class.getName());

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        try {
            SandboxTools tools = new SandboxTools();
            String result = tools.fs_edit_file(request.path, request.edits);
            return new Response(result, "Filesystem edit_file completed");
        } catch (Exception e) {
            return new Response("Error", "Filesystem edit_file error: " + e.getMessage());
        }
    }

    public record Request(
            @JsonProperty(required = true, value = "path")
            @JsonPropertyDescription("Path to the file to edit")
            String path,
            @JsonProperty(required = true, value = "edits")
            @JsonPropertyDescription("Array of edit objects with oldText and newText properties")
            Map<String, Object>[] edits
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


