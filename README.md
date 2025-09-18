### AgentScope-Runtime-Java入门

#### 安装依赖

##### 1. 安装Spring-AI-Alibaba SnapShot版本

从github上拉取[Spring-AI-Alibaba项目](https://github.com/alibaba/spring-ai-alibaba.git)，并在根目录下执行：

```shell
mvn -f "spring-ai-alibaba-bom/pom.xml" -T 1C -DskipTests install && \
\
mvn -f "pom.xml" \
  -T 1C -DskipTests -Dspotless.skip=true -Dcheckstyle.skip=true \
  -pl :spring-ai-alibaba-core,:spring-ai-alibaba-graph-core,:spring-ai-alibaba-starter-dashscope \
  -am install
```

##### 2. 安装Nacos 3.1.0 SnapShot版本

从github上拉取Nacos对应分支，并在根目录下执行：

```shell
# 1. 编译nacos-server
mvn clean install -Prelease-nacos -DskipTests=true -Drat.skip=true
# 2. 启动nacos-server
nacos_version=`cat pom.xml | grep "<revision" | cut -d ">" -f 2 | cut -d "<" -f 1`
sed -i.bak "s/^\(nacos.core.auth.plugin.nacos.token.secret.key=\)$/\1VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg=/" "distribution/target/nacos-server-$nacos_version/nacos/conf/application.properties"
sed -i.bak "s/^\(nacos.core.auth.server.identity.key=\)$/\1testKey/" "distribution/target/nacos-server-$nacos_version/nacos/conf/application.properties"
sed -i.bak "s/^\(nacos.core.auth.server.identity.value=\)$/\1testValue/" "distribution/target/nacos-server-$nacos_version/nacos/conf/application.properties"
```

##### 3. 安装docker环境

#### 启动Runtime

##### 1. 配置环境变量

配置`NACOS_USERNAME`和`NACOS_PASSWORD`环境变量用于登陆nacos，并配置`API_KEY`用于使用模型

##### 2. 启动nacos

```shell
distribution/target/nacos-server-$nacos_version/nacos/bin/startup.sh -m standalone
# 3. 待启动完成后，初始化密码
curl -X POST "127.0.0.1:8848/nacos/v3/auth/user/admin?password=$NACOS_PASSWORD"
```

启动Nacos并设置密码

##### 3. 编辑运行时配置

在`resources/application.yml`中配置了暴露端口、自定义endpoint以及应用信息等配置

在`resources/application.yml`中配置了模型的详细信息

##### 4. 编辑记忆库配置

当前的Runtime支持`内存`、`redis`以及`mysql`三种记忆库，用来实现runtime的长期记忆管理

**公有属性：**

* auto-start：是否自动启动记忆服务（默认为true）
* health-check-on-start：是否在启动时执行健康检查（默认为true）
* default-top-k：在检索记忆的时候的默认top_k值（默认为5）
* default-page-size：在调用记忆检索API的时候的默认页面大小

**memory配置属性：**

无需进行额外配置

**redis配置属性：**

示例如下：

```yaml
spring:
  data:
   redis:
     host: localhost
     port: 6379
     database: 0
     timeout: 2000ms
     lettuce:
       pool:
         max-active: 8
         max-idle: 8
         min-idle: 0
         max-wait: -1ms

  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

**mysql配置属性**（如果想要使用mysql的话，需要首先执行sql文件夹下的schema.sql语句）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/memory_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC # 数据库url配置
    username: # 数据库用户名
    password: # 数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
    open-in-view: false

  sql:
    init:
      mode: never
```

##### 5. 编辑nacos配置

```yaml
nacos:
  address: localhost:8848
  namespace: public
  username: ${NACOS_USERNAME:nacos}
  password: ${NACOS_PASSWORD}
```

##### 6. 下载沙箱

执行如下命令，从阿里云镜像源拉取对应镜像：

```shell
# 基础镜像
docker pull agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-base:latest && docker tag agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-base:latest agentscope/runtime-sandbox-base:latest

# 文件系统镜像
docker pull agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-filesystem:latest && docker tag agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-filesystem:latest agentscope/runtime-sandbox-filesystem:latest

# 浏览器镜像
docker pull agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-browser:latest && docker tag agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-browser:latest agentscope/runtime-sandbox-browser:latest
```

##### 7. 配置Agent

在`application-agent.yml`中配置Agents，Agents配置如下：

```yaml
agents: # ReactAgent/SequentialAgent/LlmRoutingAgent/
  - name: "prose_writer_agent"	# Agent名称
    type: "ReactAgent"	# Agent的type，在构建Agent的时候会根据type来构建
    description: "可以写各种散文"	# Agent的描述
    input_key: "messages"	# 输入key
    outputKey: "messages"	# 输出key
    
    instruction: "你是一个专业的作家，擅长写各种散文。请根据用户的要求创作高质量的散文。" # 给Agent中model的prompt
    max_iterations: 6	# 在Agent执行过程中思考和调用工具的最大次数

    tools: 	# 当前只支持使用沙箱中提供的工具，后续将逐步扩展范围
      - "write_file"      # 保存散文到文件
      - "read_file"       # 读取参考文件
    resolver: toolCallbackResolver    # 存在时优先于 tool，当前还未实现，可以直接使用默认

    chat_options: { }                  # 预留 Builder.chatOptions，当前还未实现
    compile_config: { }              # 预留 Builder.compileConfig，当前还未实现

    state:
      strategies:	# 针对keystrategy中的每个属性的策略，有append、replace和merge三种
        messages: append	
        response: replace

    hooks: # 预留，当前还未实现
      pre_llm: [ ]
      post_llm: [ ]
      pre_tool: [ ]
      post_tool: [ ]

    model:	# 使用的模型，可选，如果没有配置的话，会使用默认模型
      provider: dashscope
      name: qwen-max
      apiKey: ${API_KEY}
      options:
        temperature: 0.3
        top_p: 0.9
        max_tokens: 4096
        top_k: 1
        chat_client_bean: optional_bean_name  # Spring Bean 覆盖入口，当前还未实现

  - name: "RootAgent"	
    type: "LlmRoutingAgent"
    isRoot: true	# 指定是否是对外暴露的rootAgent，如果没有指定的话，如果只有一个Agent会将其指定为rootAgent，如果有多个Agent会抛出异常
    description: "助手代理"
    input_key: "input"
    outputKey: "topic"

    instruction: "助手代理"
    max_iterations: 6

    tools: 
      - "browser_navigate"     # 网页导航
    resolver: toolCallbackResolver    # 存在时优先于 tools

    chat_options: { }                  # 预留 Builder.chatOptions
    compile_config: { }              # 预留 Builder.compileConfig

    state:
      strategies:
        input: replace
        output: replace

    hooks: # 预留
      pre_llm: [ ]
      post_llm: [ ]
      pre_tool: [ ]
      post_tool: [ ]

    subAgentNames:	# 子Agent
      - "prose_writer_agent"
      - "poem_writer_agent"
      - "CodeExecuteAgent"
```

##### 8. 支持的工具列表（以下工具均支持多名称匹配）

**基础工具：**

* 运行python代码：`runpython`,`run_python`,`python`
* 运行shell脚本：`runshell`,`run_shell`,`shell`

**文件系统工具：**

* 读文件：`readfile`,`read_file`,`fs_read`
* 读多个文件：`readmultiplefiles`,`read_multiple_files`,`fs_read_multiple`
* 写文件：`writefile`,`write_file`,`fs_write`
* 编辑文件：`editfile`,`edit_file`,`fs_edit`
* 创建目录：`createdirectory`,`create_directory`,`fs_create_dir`
* 罗列目录：`listdirectory`,`list_directory`,`fs_list`
* 目录树：`directorytree`,`directory_tree`,`fs_tree`
* 移动文件：`movefile`,`move_file`,`fs_move`
* 搜索文件：`searchfiles`,`search_files`,`fs_search`
* 获取文件信息：`getfileinfo`,`get_file_info`,`fs_info`
* 列举可以访问的目录：`listalloweddirectories`,`list_allowed_directories`,`fs_allowed`

**浏览器工具：**

* 浏览器导航：`browsernavigate`,`browser_navigate`,`browser_nav`
* 浏览器点击元素：`browserclick`,`browser_click`
* 向元素中输入内容：`browsertype`,`browser_type`
* 进行截屏：`browsertakescreenshot`,`browser_take_screenshot`,`browser_screenshot`
* 捕捉浏览器快照：`browsersnapshot`,`browser_snapshot`
* 创建新的标签页：`browsertabnew`,`browser_tab_new`
* 选择标签页：`browsertabselect`,`browser_tab_select`
* 关闭标签页：`browsertabclose`,`browser_tab_close`
* 让浏览器等待：`browserwaitfor`,`browser_wait_for`
* 更改浏览器的页面大小：`browserresize`,`browser_resize`
* 关闭浏览器：`browserclose`,`browser_close`
* 获取控制台消息：`browserconsolemessages`,`browser_console_messages`
* 处理对话框：`browserhandledialog`,`browser_handle_dialog`
* 上传文件：`browserfileupload`,`browser_file_upload`
* 模拟按键：`browserpresskey`,`browser_press_key`
* 后退：`browsernavigateback`,`browser_navigate_back`
* 前进：`browsernavigateforward`,`browser_navigate_forward`
* 获取网络请求：`browsernetworkrequests`,`browser_network_requests`
* 保存为PDF：`browserpdfsave`,`browser_pdf_save`
* 拖拽元素：`browserdrag`,`browser_drag`
* 悬停元素：`browserhover`,`browser_hover`
* 选择下拉选项：`browserselectoption`,`browser_select_option`
* 列出标签页：`browsertablist`,`browser_tab_list`

