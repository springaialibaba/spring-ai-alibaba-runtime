package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器拖拽工具
 */
public class DragTool implements BiFunction<DragTool.Request, ToolContext, DragTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_drag(request.startElement, request.startRef, request.endElement, request.endRef);
        return new Response(result, "Browser drag completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "startElement")
            @JsonPropertyDescription("Human-readable source element description")
            String startElement,
            @JsonProperty(required = true, value = "startRef")
            @JsonPropertyDescription("Exact source element reference from the page snapshot")
            String startRef,
            @JsonProperty(required = true, value = "endElement")
            @JsonPropertyDescription("Human-readable target element description")
            String endElement,
            @JsonProperty(required = true, value = "endRef")
            @JsonPropertyDescription("Exact target element reference from the page snapshot")
            String endRef
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
