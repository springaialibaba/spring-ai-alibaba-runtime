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

package runtime.domain.tools.service;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Component
public class ToolsInit {

	@Value("${baidu.translate.ak:tmp}")
	private String ak;

	@Value("${baidu.translate.sk:tmp}")
	private String sk;

	@Value("${baidu.map.ak:tmp}")
	private String mapAK;

	private final RestClient.Builder restClientbuilder;

	private final ResponseErrorHandler responseErrorHandler;

	public ToolsInit(RestClient.Builder restClientbuilder, ResponseErrorHandler responseErrorHandler) {

		this.restClientbuilder = restClientbuilder;
		this.responseErrorHandler = responseErrorHandler;
	}

	public List<ToolCallback> getTools() {

		return List.of(buildMathCalculatorTools(),RunPythonCodeTools());
	}

	private ToolCallback buildMathCalculatorTools() {

		return FunctionToolCallback
				.builder(
						"MathCalculatorService",
						new MathCalculatorTools()
				).description("Perform basic mathematical calculations including addition, subtraction, multiplication, division, power, and modulo operations.")
				.inputSchema(
						"""
								{
									"type": "object",
									"properties": {
										"number1": {
											"type": "number",
											"description": "第一个数字"
										},
										"number2": {
											"type": "number",
											"description": "第二个数字"
										},
										"operation": {
											"type": "string",
											"description": "运算类型: add(加法), subtract(减法), multiply(乘法), divide(除法), power(幂运算), modulo(取模)"
										}
									},
									"required": ["number1", "number2", "operation"],
									"description": "Request object to perform mathematical calculations"
								}
								"""
				).inputType(MathCalculatorTools.MathCalculatorToolRequest.class)
				.toolMetadata(ToolMetadata.builder().returnDirect(false).build())
				.build();
	}


    private ToolCallback RunPythonCodeTools() {

        return FunctionToolCallback
                .builder(
                        "PythonExecuteService",
                        new RunPythonTools()
                ).description("Execute Python code snippets and return the output or errors.")
                .inputSchema(
                        """
                                {
                                    "type": "object",
                                    "properties": {
                                        "code": {
                                            "type": "String",
                                            "description": "Python代码"
                                        }
                                    },
                                    "required": ["code"],
                                    "description": "Request object to perform Python code execution"
                                }
                                """
                ).inputType(RunPythonTools.RunPythonToolRequest.class)
                .toolMetadata(ToolMetadata.builder().returnDirect(false).build())
                .build();
    }

}
