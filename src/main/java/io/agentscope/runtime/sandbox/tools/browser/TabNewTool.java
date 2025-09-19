/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import io.agentscope.runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

public class TabNewTool implements BiFunction<TabNewTool.Request, ToolContext, TabNewTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_tab_new(request.url);
        return new Response(result, "Browser tab_new completed");
    }

    public record Request(
            @JsonProperty("url")
            @JsonPropertyDescription("The URL to navigate to in the new tab")
            String url
    ) { 
        public Request {
            // 为可选参数提供默认值处理
            if (url == null) {
                url = "";
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


