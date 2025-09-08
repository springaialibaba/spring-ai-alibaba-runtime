package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;

public class TypeTool implements BiFunction<TypeTool.Request, ToolContext, TypeTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new BaseSandboxTools().browser_type(
                request.element, request.ref, request.text, request.submit, request.slowly);
        return new Response(result, "Browser type completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "element") String element,
            @JsonProperty(required = true, value = "ref") String ref,
            @JsonProperty(required = true, value = "text") String text,
            @JsonProperty("submit") Boolean submit,
            @JsonProperty("slowly") Boolean slowly
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


