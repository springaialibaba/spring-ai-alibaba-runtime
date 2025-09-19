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
 * 具有生命周期管理功能的服务基类
 * 结合了Service接口和生命周期管理功能，为大多数服务实现提供便利的基类
 */
public abstract class ServiceWithLifecycleManager implements Service, AutoCloseable {
    
    @Override
    public abstract CompletableFuture<Void> start();
    
    @Override
    public abstract CompletableFuture<Void> stop();
    
    @Override
    public abstract CompletableFuture<Boolean> health();
    
    @Override
    public void close() throws Exception {
        stop().get();
    }
}
