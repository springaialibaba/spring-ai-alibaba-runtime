package com.alibaba.cloud.ai.a2a.server.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class SandboxManager {
    DockerClientUtils dockerClientUtils = new DockerClientUtils();
    private Map<SandboxType, ContainerModel> sandboxMap = new HashMap<>();
    String BROWSER_SESSION_ID="123e4567-e89b-12d3-a456-426614174000";
    public DockerClient client;

    public SandboxManager() {
        client = dockerClientUtils.connectDocker();
    }

    private Map<SandboxType, String> typeNameMap= Map.of(
        SandboxType.BASE, "agentscope/runtime-sandbox-base"
    );

    public ContainerModel getSandbox(SandboxType sandboxType){
        if(sandboxMap.containsKey(sandboxType)){
            return sandboxMap.get(sandboxType);
        } else {
            DockerClientUtils dockerClientUtils = new DockerClientUtils();
            String workdir = "/workspace";
            String default_mount_dir = "sessions_mount_dir";
            String[] portsArray = {"80/tcp"};
            List<String> ports = Arrays.asList(portsArray);
            
            // 创建端口映射
            Map<String, Integer> portMapping = createPortMapping(ports);
            System.out.println("端口映射: " + portMapping);

            String imageName = "agentscope/runtime-sandbox-base";
            if(typeNameMap.containsKey(sandboxType)){
                imageName = typeNameMap.get(sandboxType);
            }

            Map<String, String> environment = new HashMap<>();
            String secretToken = RandomStringGenerator.generateRandomString(16);
            environment.put("SECRET_TOKEN", secretToken);

            String sessionId = secretToken;
            // 使用绝对路径
            String currentDir = System.getProperty("user.dir");
            String mountDir = currentDir + "/" + default_mount_dir + "/" + sessionId;
            java.io.File file = new java.io.File(mountDir);
            if (!file.exists()) {
                file.mkdirs();
            }

            List<VolumeBinding> volumeBindings = new ArrayList<>();
            volumeBindings.add(new VolumeBinding(
                    mountDir,
                    workdir,
                    "rw"
            ));

            String runtimeConfig = "runc"; // 或者 "nvidia" 等
            String containerName = "sandbox_" + sandboxType.name().toLowerCase() + "_" + sessionId;

            CreateContainerResponse container = dockerClientUtils.createContainers(
                    this.client,
                    containerName,
                    imageName,
                    ports,
                    portMapping,
                    volumeBindings,
                    environment,
                    runtimeConfig
            );

            // 将映射后的主机端口转换为字符串数组
            String[] mappedPorts = portMapping.values().stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);

            ContainerModel containerModel = ContainerModel.builder()
                    .sessionId(sessionId)
                    .containerId(container.getId())
                    .containerName(containerName)
                    .baseUrl(String.format("http://localhost:%s/fastapi", mappedPorts[0]))
                    .browserUrl(String.format("http://localhost:%s/steel-api/%s", mappedPorts[0], secretToken))
                    .frontBrowserWS(String.format("ws://localhost:%s/steel-api/%s/v1/sessions/cast", mappedPorts[0], secretToken))
                    .clientBrowserWS(String.format("ws://localhost:%s/steel-api/%s/&sessionId=%s", mappedPorts[0], secretToken, BROWSER_SESSION_ID))
                    .artifactsSIO(String.format("http://localhost:%s/v1", mappedPorts[0]))
                    .ports(mappedPorts)
                    .mountDir(workdir)
                    .authToken(secretToken)
                    .build();

            sandboxMap.put(sandboxType, containerModel);
            
            return containerModel;
        }
    }

    /**
     * 启动沙箱容器
     * @param sandboxType 沙箱类型
     */
    public void startSandbox(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            dockerClientUtils.startContainer(this.client, containerModel.getContainerId());
            System.out.println("容器状态更新为: running");
            sandboxMap.put(sandboxType, containerModel);
        }
    }

    /**
     * 停止沙箱容器
     * @param sandboxType 沙箱类型
     */
    public void stopSandbox(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            dockerClientUtils.stopContainer(this.client, containerModel.getContainerId());
            System.out.println("容器状态更新为: stopped");
            sandboxMap.put(sandboxType, containerModel);
        }
    }

    /**
     * 删除沙箱容器
     * @param sandboxType 沙箱类型
     */
    public void removeSandbox(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            DockerClientUtils dockerClientUtils = new DockerClientUtils();
            dockerClientUtils.removeContainer(this.client, containerModel.getContainerId());
            sandboxMap.remove(sandboxType);
        }
    }


    public void stopAndRemoveSandbox(SandboxType sandboxType){
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            DockerClientUtils dockerClientUtils = new DockerClientUtils();
            dockerClientUtils.stopContainer(this.client, containerModel.getContainerId());
            dockerClientUtils.removeContainer(this.client, containerModel.getContainerId());
            sandboxMap.remove(sandboxType);
        }
    }

    /**
     * 获取沙箱状态
     * @param sandboxType 沙箱类型
     * @return 沙箱状态
     */
    public String getSandboxStatus(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            DockerClientUtils dockerClientUtils = new DockerClientUtils();
            return dockerClientUtils.getContainerStatus(this.client, containerModel.getContainerId());
        }
        return "not_found";
    }

    /**
     * 获取所有沙箱信息
     * @return 沙箱映射
     */
    public Map<SandboxType, ContainerModel> getAllSandboxes() {
        return new HashMap<>(sandboxMap);
    }

    /**
     * 查找可用的端口
     * @param count 需要查找的端口数量
     * @return 可用端口列表
     */
    private List<Integer> findFreePorts(int count) {
        List<Integer> freePorts = new ArrayList<>();
        int startPort = 8000; // 从8000开始查找
        int maxAttempts = 1000; // 最多尝试1000个端口
        
        for (int i = 0; i < count && freePorts.size() < count; i++) {
            for (int port = startPort + i; port < startPort + maxAttempts; port++) {
                if (isPortAvailable(port)) {
                    freePorts.add(port);
                    break;
                }
            }
        }
        
        return freePorts;
    }

    /**
     * 检查端口是否可用
     * @param port 端口号
     * @return 是否可用
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // 端口可用
            return true;
        } catch (IOException e) {
            // 端口被占用
            return false;
        }
    }

    /**
     * 创建端口映射
     * @param containerPorts 容器端口列表
     * @return 端口映射Map
     */
    private Map<String, Integer> createPortMapping(List<String> containerPorts) {
        Map<String, Integer> portMapping = new HashMap<>();
        
        if (containerPorts != null && !containerPorts.isEmpty()) {
            List<Integer> freePorts = findFreePorts(containerPorts.size());
            
            for (int i = 0; i < containerPorts.size() && i < freePorts.size(); i++) {
                String containerPort = containerPorts.get(i);
                Integer hostPort = freePorts.get(i);
                portMapping.put(containerPort, hostPort);
            }
        }
        
        return portMapping;
    }

}
