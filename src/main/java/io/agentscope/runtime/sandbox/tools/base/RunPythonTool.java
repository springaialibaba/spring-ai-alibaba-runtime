/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.agentscope.runtime.sandbox.tools.base;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import io.agentscope.runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * 调用沙箱运行python代码
 *
 * @author xuehuitian45
 * @since 2025/9/4
 */
public class RunPythonTool implements BiFunction<RunPythonTool.RunPythonToolRequest, ToolContext, RunPythonTool.RunPythonToolResponse> {

    Logger logger = Logger.getLogger(RunPythonTool.class.getName());

    @Override
    public RunPythonToolResponse apply(RunPythonToolRequest request, ToolContext toolContext) {
        try {
            String result = performPythonExecute(
                    request.code
            );

            return new RunPythonToolResponse(
                    new Response(result, "Code execution completed")
            );
        } catch (Exception e) {
            return new RunPythonToolResponse(
                    new Response("Error", "Code execution error : " + e.getMessage())
            );
        }
    }


    private String performPythonExecute(String code) {
        logger.info("Run Code: " + code);
        SandboxTools tools = new SandboxTools();
        String result = tools.run_ipython_cell(code);
        logger.info("Execute Result: " + result);
        return result;
    }

    // 请求类型定义
    public record RunPythonToolRequest(
            @JsonProperty(required = true, value = "code")
            @JsonPropertyDescription("Python code to be executed")
            String code
    ) {
        public RunPythonToolRequest(String code) {
            this.code = code;
        }
    }

    // 响应类型定义
    public record RunPythonToolResponse(@JsonProperty("Response") Response output) {
        public RunPythonToolResponse(Response output) {
            this.output = output;
        }
    }



    @JsonClassDescription("The result contains the code output and the execute result")
    public record Response(String result, String message) {
        public Response(String result, String message) {
            this.result = result;
            this.message = message;
        }

        @JsonProperty(required = true, value = "result")
        @JsonPropertyDescription("code output")
        public String result() {
            return this.result;
        }

        @JsonProperty(required = true, value = "message")
        @JsonPropertyDescription("execute result")
        public String message() {
            return this.message;
        }
    }
}
