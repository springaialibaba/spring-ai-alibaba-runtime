package runtime.sandbox.manager;

import runtime.sandbox.manager.model.DockerProp;
import runtime.sandbox.manager.model.VolumeBinding;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.*;

/**
 * @Auther: shaonaiyi@163.com
 * @Date: 2021/1/10 15:37
 * @Description: Java API实现创建Docker容器
 */
public class DockerManager {

    /**
     * 连接Docker服务器（Mac默认连接方式）
     *
     * @return
     */
    public DockerClient connectDocker() {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        dockerClient.infoCmd().exec();
        return dockerClient;
    }

    /**
     * 连接Docker服务器（指定连接地址）
     *
     * @param dockerInstance Docker连接地址
     * @return
     */
    public DockerClient connectDocker(String dockerInstance) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(dockerInstance).build();
        dockerClient.infoCmd().exec();
        return dockerClient;
    }

    /**
     * 创建容器（简单版本）
     *
     * @param client        Docker客户端
     * @param containerName 容器名称
     * @param imageName     镜像名称
     * @return
     */
    public CreateContainerResponse createContainers(DockerClient client, String containerName, String imageName) {
        CreateContainerResponse container = client.createContainerCmd(imageName)
                .withName(containerName)
                .exec();
        return container;
    }

    /**
     * 创建容器（使用DockerProp配置，支持端口绑定）
     *
     * @param client     Docker客户端
     * @param dockerProp Docker配置属性
     * @return
     */
    public CreateContainerResponse createContainers(DockerClient client, DockerProp dockerProp) {
        // 端口绑定
        Map<Integer, Integer> portMap = Optional.ofNullable(dockerProp).map(DockerProp::getPartMap).orElse(new HashMap<>());
        Iterator<Map.Entry<Integer, Integer>> iterator = portMap.entrySet().iterator();
        List<PortBinding> portBindingList = new ArrayList<>();
        List<ExposedPort> exposedPortList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            ExposedPort tcp = ExposedPort.tcp(entry.getKey());
            Ports.Binding binding = Ports.Binding.bindPort(entry.getValue());
            PortBinding ports = new PortBinding(binding, tcp);
            portBindingList.add(ports);
            exposedPortList.add(tcp);
        }

        CreateContainerResponse container = client.createContainerCmd(dockerProp.getImageName())
                .withName(dockerProp.getContainerName())
                .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindingList))
                .withExposedPorts(exposedPortList).exec();

        return container;
    }

    /**
     * 创建容器（完整版本，支持端口映射、卷挂载、环境变量等）
     *
     * @param client         Docker客户端
     * @param containerName  容器名称
     * @param imageName      镜像名称
     * @param ports          端口映射列表，格式如["80/tcp", "443/tcp"]
     * @param volumeBindings 卷挂载映射
     * @param environment    环境变量
     * @param runtimeConfig  运行时配置
     * @return
     */
    public CreateContainerResponse createContainers(DockerClient client, String containerName, String imageName,
                                                    List<String> ports, Map<String, String> volumeBindings,
                                                    Map<String, String> environment, String runtimeConfig) {

        CreateContainerCmd createCmd = client.createContainerCmd(imageName)
                .withName(containerName);

        // 设置端口映射
        if (ports != null && !ports.isEmpty()) {
            ExposedPort[] exposedPorts = new ExposedPort[ports.size()];

            for (int i = 0; i < ports.size(); i++) {
                String[] portParts = ports.get(i).split("/");
                int port = Integer.parseInt(portParts[0]);
                String protocol = portParts.length > 1 ? portParts[1] : "tcp";

                ExposedPort exposedPort = ExposedPort.tcp(port);
                if ("udp" .equals(protocol)) {
                    exposedPort = ExposedPort.udp(port);
                }
                exposedPorts[i] = exposedPort;
            }

            createCmd.withExposedPorts(exposedPorts);
        }

        // 设置卷挂载
        if (volumeBindings != null && !volumeBindings.isEmpty()) {
            Volume[] volumes = new Volume[volumeBindings.size()];
            Bind[] binds = new Bind[volumeBindings.size()];

            int index = 0;
            for (Map.Entry<String, String> entry : volumeBindings.entrySet()) {
                String hostPath = entry.getKey();
                String containerPath = entry.getValue();

                Volume volume = new Volume(containerPath);
                volumes[index] = volume;
                binds[index] = new Bind(hostPath, volume);
                index++;
            }

            // 使用HostConfig设置卷挂载（推荐方式）
            HostConfig hostConfig = new HostConfig()
                    .withBinds(binds);

            createCmd.withVolumes(volumes);
            createCmd.withHostConfig(hostConfig);
        }

        // 设置环境变量
        if (environment != null && !environment.isEmpty()) {
            String[] envArray = new String[environment.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                envArray[index] = entry.getKey() + "=" + entry.getValue();
                index++;
            }
            createCmd.withEnv(envArray);
        }

        // 设置运行时配置
        if (runtimeConfig != null && !runtimeConfig.isEmpty()) {
            // 注意：withRuntime方法在当前版本中可能不可用
            // 这里保留参数以便将来扩展
            System.out.println("运行时配置: " + runtimeConfig + " (当前版本暂不支持)");
        }

        return createCmd.exec();
    }

    /**
     * 创建容器（使用VolumeBinding格式）
     *
     * @param client         Docker客户端
     * @param containerName  容器名称
     * @param imageName      镜像名称
     * @param ports          端口映射列表，格式如["80/tcp", "443/tcp"]
     * @param volumeBindings 卷挂载映射，使用VolumeBinding对象
     * @param environment    环境变量
     * @param runtimeConfig  运行时配置
     * @return
     */
    public CreateContainerResponse createContainers(DockerClient client, String containerName, String imageName,
                                                    List<String> ports, List<VolumeBinding> volumeBindings,
                                                    Map<String, String> environment, String runtimeConfig) {

        CreateContainerCmd createCmd = client.createContainerCmd(imageName)
                .withName(containerName);

        // 设置端口映射
        if (ports != null && !ports.isEmpty()) {
            ExposedPort[] exposedPorts = new ExposedPort[ports.size()];

            for (int i = 0; i < ports.size(); i++) {
                String[] portParts = ports.get(i).split("/");
                int port = Integer.parseInt(portParts[0]);
                String protocol = portParts.length > 1 ? portParts[1] : "tcp";

                ExposedPort exposedPort = ExposedPort.tcp(port);
                if ("udp" .equals(protocol)) {
                    exposedPort = ExposedPort.udp(port);
                }
                exposedPorts[i] = exposedPort;
            }

            createCmd.withExposedPorts(exposedPorts);
        }

        // 设置卷挂载（使用VolumeBinding）
        if (volumeBindings != null && !volumeBindings.isEmpty()) {
            Volume[] volumes = new Volume[volumeBindings.size()];
            Bind[] binds = new Bind[volumeBindings.size()];

            for (int i = 0; i < volumeBindings.size(); i++) {
                VolumeBinding binding = volumeBindings.get(i);
                Volume volume = new Volume(binding.getContainerPath());
                volumes[i] = volume;

                // 根据mode设置读写权限
                if ("ro" .equals(binding.getMode())) {
                    binds[i] = new Bind(binding.getHostPath(), volume, AccessMode.ro);
                } else {
                    binds[i] = new Bind(binding.getHostPath(), volume, AccessMode.rw);
                }
            }

            // 使用HostConfig设置卷挂载（推荐方式）
            HostConfig hostConfig = new HostConfig()
                    .withBinds(binds);

            createCmd.withVolumes(volumes);
            createCmd.withHostConfig(hostConfig);
        }

        // 设置环境变量
        if (environment != null && !environment.isEmpty()) {
            String[] envArray = new String[environment.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                envArray[index] = entry.getKey() + "=" + entry.getValue();
                index++;
            }
            createCmd.withEnv(envArray);
        }

        // 设置运行时配置
        if (runtimeConfig != null && !runtimeConfig.isEmpty()) {
            // 注意：withRuntime方法在当前版本中可能不可用
            // 这里保留参数以便将来扩展
            System.out.println("运行时配置: " + runtimeConfig + " (当前版本暂不支持)");
        }

        return createCmd.exec();
    }

    /**
     * 创建容器（支持端口映射）
     *
     * @param client         Docker客户端
     * @param containerName  容器名称
     * @param imageName      镜像名称
     * @param ports          端口映射列表，格式如["80/tcp", "443/tcp"]
     * @param portMapping    端口映射Map，key为容器端口，value为主机端口
     * @param volumeBindings 卷挂载映射，使用VolumeBinding对象
     * @param environment    环境变量
     * @param runtimeConfig  运行时配置
     * @return
     */
    public CreateContainerResponse createContainers(DockerClient client, String containerName, String imageName,
                                                    List<String> ports, Map<String, Integer> portMapping,
                                                    List<VolumeBinding> volumeBindings,
                                                    Map<String, String> environment, String runtimeConfig) {

        CreateContainerCmd createCmd = client.createContainerCmd(imageName)
                .withName(containerName);
        HostConfig hostConfig = HostConfig.newHostConfig();

        // 设置端口映射
        if (ports != null && !ports.isEmpty() && portMapping != null && !portMapping.isEmpty()) {
            List<PortBinding> list = new ArrayList<>();
            List<ExposedPort> exposedPorts = new ArrayList<>();
            
            for (String containerPort : portMapping.keySet()) {
                Integer hostPort = portMapping.get(containerPort);
                // 暴露容器端口
                ExposedPort exposedPort = ExposedPort.parse(containerPort);
                exposedPorts.add(exposedPort);
                // 绑定主机端口 -> 容器端口
                list.add(PortBinding.parse(hostPort + ":" + containerPort));
            }
            
            createCmd = createCmd.withExposedPorts(exposedPorts);
            hostConfig = hostConfig.withPortBindings(list);
        }

        // 设置卷挂载（使用VolumeBinding）
        if (volumeBindings != null && !volumeBindings.isEmpty()) {
            Volume[] volumes = new Volume[volumeBindings.size()];
            Bind[] binds = new Bind[volumeBindings.size()];

            for (int i = 0; i < volumeBindings.size(); i++) {
                VolumeBinding binding = volumeBindings.get(i);
                Volume volume = new Volume(binding.getContainerPath());
                volumes[i] = volume;

                // 根据mode设置读写权限
                if ("ro" .equals(binding.getMode())) {
                    binds[i] = new Bind(binding.getHostPath(), volume, AccessMode.ro);
                } else {
                    binds[i] = new Bind(binding.getHostPath(), volume, AccessMode.rw);
                }
            }

            hostConfig = hostConfig.withBinds(binds);

            createCmd.withVolumes(volumes);
        }
        createCmd.withHostConfig(hostConfig);

        // 设置环境变量
        if (environment != null && !environment.isEmpty()) {
            String[] envArray = new String[environment.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                envArray[index] = entry.getKey() + "=" + entry.getValue();
                index++;
            }
            createCmd.withEnv(envArray);
        }

        // 设置运行时配置
        if (runtimeConfig != null && !runtimeConfig.isEmpty()) {
            // 注意：withRuntime方法在当前版本中可能不可用
            // 这里保留参数以便将来扩展
            System.out.println("运行时配置: " + runtimeConfig + " (当前版本暂不支持)");
        }

        return createCmd.exec();
    }


    /**
     * 启动容器
     *
     * @param client      Docker客户端
     * @param containerId 容器ID
     */
    public void startContainer(DockerClient client, String containerId) {
        try {
            client.startContainerCmd(containerId).exec();
            System.out.println("容器启动成功: " + containerId);
        } catch (Exception e) {
            System.err.println("启动容器失败: " + e.getMessage());
        }
    }

    /**
     * 停止容器
     *
     * @param client      Docker客户端
     * @param containerId 容器ID
     */
    public void stopContainer(DockerClient client, String containerId) {
        try {
            // 先检查容器状态
            String status = getContainerStatus(client, containerId);
            if ("running" .equals(status)) {
                client.stopContainerCmd(containerId).exec();
                System.out.println("容器停止成功: " + containerId);
            } else {
                System.out.println("容器已经停止，状态: " + status);
            }
        } catch (Exception e) {
            System.err.println("停止容器失败: " + e.getMessage());
        }
    }

    /**
     * 删除容器
     *
     * @param client      Docker客户端
     * @param containerId 容器ID
     */
    public void removeContainer(DockerClient client, String containerId) {
        try {
            // 强制删除容器，即使容器正在运行
            client.removeContainerCmd(containerId)
                    .withForce(true)  // 强制删除
                    .withRemoveVolumes(true)  // 同时删除关联的卷
                    .exec();
            System.out.println("容器删除成功: " + containerId);
        } catch (Exception e) {
            System.err.println("删除容器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取容器状态
     *
     * @param client      Docker客户端
     * @param containerId 容器ID
     * @return 容器状态
     */
    public String getContainerStatus(DockerClient client, String containerId) {
        try {
            InspectContainerResponse response = client.inspectContainerCmd(containerId).exec();
            return response.getState().getStatus();
        } catch (Exception e) {
            System.err.println("获取容器状态失败: " + e.getMessage());
            return "unknown";
        }
    }

    /**
     * 停止并删除容器
     *
     * @param client      Docker客户端
     * @param containerId 容器ID
     */
    public void stopAndRemoveContainer(DockerClient client, String containerId) {
        this.stopContainer(client, containerId);
        this.removeContainer(client, containerId);
    }
}

