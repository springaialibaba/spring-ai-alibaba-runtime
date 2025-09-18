package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

public class NavigateTool implements BiFunction<NavigateTool.Request, ToolContext, NavigateTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_navigate(request.url);
        return new Response(result, "Browser navigate completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "url")
            @JsonPropertyDescription("The URL to navigate to")
            String url
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


