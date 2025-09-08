package runtime.domain.tools.service.fs;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;
import java.util.logging.Logger;

public class SearchFilesTool implements BiFunction<SearchFilesTool.Request, ToolContext, SearchFilesTool.Response> {

    Logger logger = Logger.getLogger(SearchFilesTool.class.getName());

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        try {
            BaseSandboxTools tools = new BaseSandboxTools();
            String result = tools.fs_search_files(request.path, request.pattern, request.excludePatterns);
            return new Response(result, "Filesystem search_files completed");
        } catch (Exception e) {
            return new Response("Error", "Filesystem search_files error: " + e.getMessage());
        }
    }

    public record Request(
            @JsonProperty(required = true, value = "path")
            @JsonPropertyDescription("Starting path for the search")
            String path,
            @JsonProperty(required = true, value = "pattern")
            @JsonPropertyDescription("Pattern to match files/directories")
            String pattern,
            @JsonProperty(value = "excludePatterns")
            @JsonPropertyDescription("Patterns to exclude from search")
            String[] excludePatterns
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


