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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

/**
 * 数学计算工具类 - 提供基本的数学运算功能
 * 
 * @author yingzi
 * @since 2025/1/15
 */
public class MathCalculatorTools implements BiFunction<MathCalculatorTools.MathCalculatorToolRequest, ToolContext, MathCalculatorTools.MathCalculatorToolResponse> {

    @Override
    public MathCalculatorToolResponse apply(MathCalculatorToolRequest request, ToolContext toolContext) {
        try {
            double result = performCalculation(
                request.number1, 
                request.number2, 
                request.operation
            );
            
            return new MathCalculatorToolResponse(
                new Response(result, "计算成功")
            );
        } catch (Exception e) {
            return new MathCalculatorToolResponse(
                new Response(0.0, "计算失败: " + e.getMessage())
            );
        }
    }

    /**
     * 执行数学计算
     */
    private double performCalculation(double number1, double number2, String operation) {
        return switch (operation.toLowerCase()) {
            case "add", "加法", "+" -> number1 + number2;
            case "subtract", "减法", "-" -> number1 - number2;
            case "multiply", "乘法", "*" -> number1 * number2;
            case "divide", "除法", "/" -> {
                if (number2 == 0) {
                    throw new IllegalArgumentException("除数不能为零");
                }
                yield number1 / number2;
            }
            case "power", "幂运算", "^" -> Math.pow(number1, number2);
            case "modulo", "取模", "%" -> number1 % number2;
            default -> throw new IllegalArgumentException("不支持的操作: " + operation);
        };
    }

    // 请求类型定义
    public record MathCalculatorToolRequest(
        @JsonProperty(required = true, value = "number1") 
        @JsonPropertyDescription("第一个数字") 
        double number1,
        @JsonProperty(required = true, value = "number2") 
        @JsonPropertyDescription("第二个数字") 
        double number2,
        @JsonProperty(required = true, value = "operation") 
        @JsonPropertyDescription("运算类型: add(加法), subtract(减法), multiply(乘法), divide(除法), power(幂运算), modulo(取模)") 
        String operation
    ) {
        public MathCalculatorToolRequest(double number1, double number2, String operation) {
            this.number1 = number1;
            this.number2 = number2;
            this.operation = operation;
        }
    }

    // 响应类型定义
    public record MathCalculatorToolResponse(@JsonProperty("Response") Response output) {
        public MathCalculatorToolResponse(Response output) {
            this.output = output;
        }
    }



    @JsonClassDescription("Response containing calculation result")
    public record Response(double result, String message) {
        public Response(double result, String message) {
            this.result = result;
            this.message = message;
        }

        @JsonProperty(required = true, value = "result")
        @JsonPropertyDescription("计算结果")
        public double result() {
            return this.result;
        }

        @JsonProperty(required = true, value = "message")
        @JsonPropertyDescription("操作结果消息")
        public String message() {
            return this.message;
        }
    }
}
