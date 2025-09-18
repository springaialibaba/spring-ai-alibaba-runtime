package runtime.engine.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务管理器抽象基类
 * 提供服务注册和生命周期管理的通用功能
 */
public abstract class ServiceManager implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);
    
    private final List<ServiceRegistration> services = new ArrayList<>();
    private final Map<String, Service> serviceInstances = new ConcurrentHashMap<>();
    
    public ServiceManager() {
        // 初始化默认服务
        registerDefaultServices();
    }
    
    /**
     * 注册此管理器的默认服务，在子类中重写
     */
    protected abstract void registerDefaultServices();
    
    /**
     * 注册服务
     * 
     * @param serviceClass 要注册的服务类
     * @param name 可选的服务名称，默认为类名去掉"Service"后缀并转为小写
     * @param args 服务初始化的位置参数
     * @return this 用于方法链式调用
     */
    public ServiceManager register(Class<? extends Service> serviceClass, String name, Object... args) {
        if (name == null) {
            name = serviceClass.getSimpleName().replace("Service", "").toLowerCase();
        }
        
        // 检查服务名称是否已存在
        if (serviceInstances.containsKey(name)) {
            throw new IllegalArgumentException("Service with name '" + name + "' is already registered");
        }
        
        services.add(new ServiceRegistration(serviceClass, name, args));
        logger.debug("Registered service: {} ({})", name, serviceClass.getSimpleName());
        return this;
    }
    
    /**
     * 注册已实例化的服务
     * 
     * @param name 服务名称
     * @param service 服务实例
     * @return this 用于方法链式调用
     */
    public ServiceManager registerService(String name, Service service) {
        if (serviceInstances.containsKey(name)) {
            throw new IllegalArgumentException("Service with name '" + name + "' is already registered");
        }
        
        serviceInstances.put(name, service);
        logger.debug("Registered service instance: {}", name);
        return this;
    }
    
    /**
     * 启动所有注册的服务
     * 
     * @return CompletableFuture<Void> 异步启动结果
     */
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 启动通过register()注册的服务
                for (ServiceRegistration registration : services) {
                    logger.debug("Starting service: {}", registration.name);
                    try {
                        Service instance = registration.serviceClass.getDeclaredConstructor()
                                .newInstance();
                        instance.start().get();
                        serviceInstances.put(registration.name, instance);
                        logger.debug("Successfully started service: {}", registration.name);
                    } catch (Exception e) {
                        logger.error("Failed to start service: {}", registration.name, e);
                        throw new RuntimeException("Failed to start service: " + registration.name, e);
                    }
                }
                
                // 启动通过registerService()注册的服务
                for (Map.Entry<String, Service> entry : serviceInstances.entrySet()) {
                    String name = entry.getKey();
                    Service service = entry.getValue();
                    if (!services.stream().anyMatch(reg -> reg.name.equals(name))) {
                        logger.debug("Starting pre-instantiated service: {}", name);
                        try {
                            service.start().get();
                            logger.debug("Successfully started pre-instantiated service: {}", name);
                        } catch (Exception e) {
                            logger.error("Failed to start pre-instantiated service: {}", name, e);
                            throw new RuntimeException("Failed to start pre-instantiated service: " + name, e);
                        }
                    }
                }
                
            } catch (Exception e) {
                logger.error("Failed to start services", e);
                // 确保在初始化失败时进行适当的清理
                stop().join();
                throw new RuntimeException("Failed to start services", e);
            }
        });
    }
    
    /**
     * 停止所有服务
     * 
     * @return CompletableFuture<Void> 异步停止结果
     */
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            logger.debug("Stopping all services");
            for (Service service : serviceInstances.values()) {
                try {
                    service.stop().get();
                } catch (Exception e) {
                    logger.error("Error stopping service", e);
                }
            }
            serviceInstances.clear();
            logger.debug("All services stopped");
        });
    }
    
    @Override
    public void close() throws Exception {
        stop().get();
    }
    
    /**
     * 启用服务的属性访问，例如manager.env, manager.session
     */
    public Service getService(String name) {
        Service service = serviceInstances.get(name);
        if (service == null) {
            throw new IllegalArgumentException("Service '" + name + "' not found");
        }
        return service;
    }
    
    /**
     * 显式检索服务实例，支持默认值
     */
    public Service getService(String name, Service defaultService) {
        return serviceInstances.getOrDefault(name, defaultService);
    }
    
    /**
     * 检查服务是否存在
     */
    public boolean hasService(String name) {
        return serviceInstances.containsKey(name);
    }
    
    /**
     * 列出所有注册的服务名称
     */
    public List<String> listServices() {
        return new ArrayList<>(serviceInstances.keySet());
    }
    
    /**
     * 检索所有服务实例
     */
    public Map<String, Service> getAllServices() {
        return new HashMap<>(serviceInstances);
    }
    
    /**
     * 检查所有服务的健康状态
     * 
     * @return CompletableFuture<Map<String, Boolean>> 异步健康检查结果
     */
    public CompletableFuture<Map<String, Boolean>> healthCheck() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Boolean> healthStatus = new HashMap<>();
            for (Map.Entry<String, Service> entry : serviceInstances.entrySet()) {
                String name = entry.getKey();
                Service service = entry.getValue();
                try {
                    healthStatus.put(name, service.health().get());
                } catch (Exception e) {
                    logger.error("Health check failed for service {}", name, e);
                    healthStatus.put(name, false);
                }
            }
            return healthStatus;
        });
    }
    
    /**
     * 服务注册信息内部类
     */
    private static class ServiceRegistration {
        final Class<? extends Service> serviceClass;
        final String name;
        final Object[] args;
        
        ServiceRegistration(Class<? extends Service> serviceClass, String name, Object[] args) {
            this.serviceClass = serviceClass;
            this.name = name;
            this.args = args;
        }
    }
}
