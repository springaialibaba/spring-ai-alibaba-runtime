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

package com.alibaba.cloud.ai.a2a.server.tools;

import com.alibaba.cloud.ai.a2a.server.sandbox.tools.BaseSandboxTools;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

/**
 * 调用沙箱运行python代码
 *
 * @author xuehuitian45
 * @since 2025/9/4
 */
public class RunPythonTools implements BiFunction<RunPythonTools.RunPythonToolRequest, ToolContext, RunPythonTools.RunPythonToolResponse> {

    @Override
    public RunPythonToolResponse apply(RunPythonToolRequest request, ToolContext toolContext) {
        System.out.println("运行函数工具被调用，收到请求: " + request.code);
        try {
            String result = performPythonExecute(
                    request.code
            );

            return new RunPythonToolResponse(
                    new Response(result, "代码执行完成")
            );
        } catch (Exception e) {
            return new RunPythonToolResponse(
                    new Response("Error", "代码执行失败: " + e.getMessage())
            );
        }
    }

    /**
     * 执行数学计算
     */
    private String performPythonExecute(String code) {
        System.out.println("执行代码: " + code);
        BaseSandboxTools tools = new BaseSandboxTools();
        String result = tools.run_ipython_cell(code);
        System.out.println("执行结果: " + result);
        return result;
    }

    // 请求类型定义
    public record RunPythonToolRequest(
            @JsonProperty(required = true, value = "code")
            @JsonPropertyDescription("代码内容")
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



    @JsonClassDescription("结果中包含计算结果和操作结果消息")
    public record Response(String result, String message) {
        public Response(String result, String message) {
            this.result = result;
            this.message = message;
        }

        @JsonProperty(required = true, value = "result")
        @JsonPropertyDescription("代码输出")
        public String result() {
            return this.result;
        }

        @JsonProperty(required = true, value = "message")
        @JsonPropertyDescription("操作结果消息")
        public String message() {
            return this.message;
        }
    }
}
