### 独立运行指令

前置要求：JDK 17、Maven 3.9+、可访问 Maven 中央仓库、Nacos 3.1.0+。

- 必要环境变量：`AI_DASHSCOPE_API_KEY`（用于 `application-agent.yml` 中的 DashScope 配置）。
- 必要环境变量：`NACOS_PASSWORD`(用于 `application-nacos.yml` 中的 Nacos 配置）。

#### 1) 安装外部依赖模块（含 BOM）

在spring-ai-alibaba根目录下运行：
```bash
mvn -f "spring-ai-alibaba-bom/pom.xml" -T 1C -DskipTests install && \
\
# 2. 安装本服务所需的外部模块（核心与图引擎、DashScope Starter）
mvn -f "pom.xml" \
  -T 1C -DskipTests -Dspotless.skip=true -Dcheckstyle.skip=true \
  -pl :spring-ai-alibaba-core,:spring-ai-alibaba-graph-core,:spring-ai-alibaba-starter-dashscope \
  -am install
```

在nacos根目录下运行：
```bash 
# 1. 编译nacos-server
mvn clean install -Prelease-nacos -DskipTests=true -Drat.skip=true
# 2. 启动nacos-server
nacos_version=`cat pom.xml | grep "<revision" | cut -d ">" -f 2 | cut -d "<" -f 1`
sed -i.bak "s/^\(nacos.core.auth.plugin.nacos.token.secret.key=\)$/\1VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg=/" "distribution/target/nacos-server-$nacos_version/nacos/conf/application.properties"
sed -i.bak "s/^\(nacos.core.auth.server.identity.key=\)$/\1testKey/" "distribution/target/nacos-server-$nacos_version/nacos/conf/application.properties"
sed -i.bak "s/^\(nacos.core.auth.server.identity.value=\)$/\1testValue/" "distribution/target/nacos-server-$nacos_version/nacos/conf/application.properties"
distribution/target/nacos-server-$nacos_version/nacos/bin/startup.sh -m standalone
# 3. 待启动完成后，初始化密码
curl -X POST "127.0.0.1:8848/nacos/v3/auth/user/admin?password=$NACOS_PASSWORD"
```

#### 2) 运行
```bash
export AI_DASHSCOPE_API_KEY="sk-xxxxxxxx" && \
mvn -f "pom.xml" -Dspring-boot.run.arguments="--server.port=8080" spring-boot:run
```

#### 3) 健康检查与接口

使用spring-ai-alibaba中的RemoteAgentTest来进行测试