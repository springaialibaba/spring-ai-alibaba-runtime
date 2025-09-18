package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

public class TypeTool implements BiFunction<TypeTool.Request, ToolContext, TypeTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_type(
                request.element, request.ref, request.text, request.submit, request.slowly);
        return new Response(result, "Browser type completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "element") String element,
            @JsonProperty(required = true, value = "ref") String ref,
            @JsonProperty(required = true, value = "text") String text,
            @JsonProperty("submit") Boolean submit,
            @JsonProperty("slowly") Boolean slowly
    ) { 
        public Request {
            // 为可选参数提供默认值处理
            if (submit == null) {
                submit = false;
            }
            if (slowly == null) {
                slowly = false;
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


