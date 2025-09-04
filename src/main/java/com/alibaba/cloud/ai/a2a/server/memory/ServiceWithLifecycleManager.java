package com.alibaba.cloud.ai.a2a.server.memory;

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
