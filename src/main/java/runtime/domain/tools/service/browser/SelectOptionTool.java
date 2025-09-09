package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器下拉选择工具
 */
public class SelectOptionTool implements BiFunction<SelectOptionTool.Request, ToolContext, SelectOptionTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_select_option(request.element, request.ref, request.values);
        return new Response(result, "Browser select option completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "element")
            @JsonPropertyDescription("Human-readable element description")
            String element,
            @JsonProperty(required = true, value = "ref")
            @JsonPropertyDescription("Exact target element reference from the page snapshot")
            String ref,
            @JsonProperty(required = true, value = "values")
            @JsonPropertyDescription("Array of values to select in the dropdown")
            String[] values
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
