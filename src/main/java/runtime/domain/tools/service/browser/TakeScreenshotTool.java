package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

public class TakeScreenshotTool implements BiFunction<TakeScreenshotTool.Request, ToolContext, TakeScreenshotTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_take_screenshot(
                request.raw, request.filename, request.element, request.ref);
        return new Response(result, "Browser take_screenshot completed");
    }

    public record Request(
            @JsonProperty("raw") Boolean raw,
            @JsonProperty("filename") String filename,
            @JsonProperty("element") String element,
            @JsonProperty("ref") String ref
    ) { 
        public Request {
            // 为可选参数提供默认值处理
            if (raw == null) {
                raw = false;
            }
            if (filename == null) {
                filename = "";
            }
            if (element == null) {
                element = "";
            }
            if (ref == null) {
                ref = "";
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


