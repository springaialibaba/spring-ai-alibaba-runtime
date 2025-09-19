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
package io.agentscope.runtime.engine.shared;

import java.util.concurrent.CompletableFuture;

/**
 * 服务接口，定义所有服务必须实现的基本方法
 */
public interface Service {
    
    /**
     * 启动服务，初始化必要的资源或连接
     * 
     * @return CompletableFuture<Void> 异步启动结果
     */
    CompletableFuture<Void> start();
    
    /**
     * 停止服务，释放已获取的资源
     * 
     * @return CompletableFuture<Void> 异步停止结果
     */
    CompletableFuture<Void> stop();
    
    /**
     * 检查服务的健康状态
     * 
     * @return CompletableFuture<Boolean> 异步健康检查结果，true表示健康，false表示不健康
     */
    CompletableFuture<Boolean> health();
}
