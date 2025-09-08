package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;

public class ResizeTool implements BiFunction<ResizeTool.Request, ToolContext, ResizeTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new BaseSandboxTools().browser_resize(request.width, request.height);
        return new Response(result, "Browser resize completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "width")
            @JsonPropertyDescription("Width of the browser window")
            Double width,
            @JsonProperty(required = true, value = "height")
            @JsonPropertyDescription("Height of the browser window")
            Double height
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


