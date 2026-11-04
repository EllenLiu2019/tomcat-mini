# tomcat-mini

<p align="center"><a href="README.en.md">English</a> | 中文简体</p>

参照 Tomcat 源码结构、用 Java 17 手写的简化版 Servlet 容器，用于理解 Tomcat 的核心设计：连接器与处理器池、Host / Context / Wrapper 容器层级、Pipeline / Valve 职责链、Filter 与 Listener、Session，以及自定义类加载器。

## 已实现的功能

- **连接器（Connector）**：BIO（Blocking I/O）模式，用 `ServerSocket` 监听端口。启动时创建 3 个 `HttpProcessor` 线程，按需增加到最多 10 个；没有空闲线程且已达上限时，直接关闭新连接。连接器线程和处理器线程之间通过 `wait()` / `notifyAll()` 传递 Socket。
- **HTTP 解析**：解析请求行、请求头和 Cookie。调用 Servlet 时传入 Facade 对象（`HttpRequestFacade` / `HttpResponseFacade`），Servlet 无法直接调用容器内部的方法。
- **Session**：Session ID 通过 Cookie（`jsessionid`）或 URL 中的 `;jsessionid=` 传递，Session 保存在内存中。
- **容器层级**：`StandardHost` → `StandardContext` → `StandardWrapper`。启动时，`webapps/` 下的每个子目录对应一个 Context（一个 Web 应用）；每个 Wrapper 封装一个 Servlet 实例。
- **Pipeline / Valve 职责链**：每一层容器都有一个 Pipeline。Pipeline 依次调用其中的 Valve，最后调用 Basic Valve；Basic Valve 找到下一层容器并继续调用。
- **Filter 链**：根据 `web.xml` 的 `filter-mapping` 为每个请求创建 `ApplicationFilterChain`，依次执行 Filter，最后调用 Servlet。
- **Listener**：Host 启动时发布容器事件，由 `HostListener` 接收；各应用的 Listener 按 `web.xml` 注册。
- **配置文件**：启动时读取 `conf/server.xml`；每个应用启动时读取自己的 `WEB-INF/web.xml`（`listener`、`filter`、`filter-mapping`、`servlet`）。设置了 `load-on-startup` 的 Servlet 在应用启动时创建，其他 Servlet 在第一次请求时创建。
- **自定义类加载器**：`CommonClassLoader`（所有应用共用）和 `WebappClassLoader`（每个应用一个），加载顺序与标准的双亲委派不同，见[类加载](#类加载)。
- **静态资源**：非 Servlet 请求按路径返回应用目录下的文件。
- **日志**：`FileLogger` 按日期把容器日志写到 `logs/` 目录。



## 请求处理流程

```text
HttpConnector                 监听端口，把 Socket 交给空闲的 HttpProcessor
└─ HttpProcessor              解析请求；应用名之后的路径以 /servlet/ 开头时交给容器，否则按静态文件处理
   └─ StandardHost            Pipeline → StandardHostValve：按 URL 第一段（如 app1）找到 Context
      └─ StandardContext      Pipeline → StandardContextValve：按 URL 最后一段找到 Wrapper
         └─ StandardWrapper   Pipeline → StandardWrapperValve：创建 Filter 链
            └─ Filter → ... → Servlet.service()
```



## URL 规则

- `/<应用名>/servlet/<类名>`：调用 Servlet `<应用名>.<类名>`。例如 `/app1/servlet/TestServlet` 对应 `app1.TestServlet`。
- 其他路径：返回 `webapps/<应用名>/` 下的文件。例如 `/app1/hello.txt` 对应 `webapps/app1/hello.txt`。



## 类加载

```text
系统类加载器（应用类路径）
└─ CommonClassLoader          所有应用共用
   ├─ WebappClassLoader       app1
   └─ WebappClassLoader       app2
```

`loadClass()` 的查找顺序（`delegate` 默认为 `false`）：

1. 先交给系统类加载器，防止应用中的类覆盖 JDK 的类；
2. 在自己的目录中查找；
3. 还找不到，再交给父加载器。

`delegate` 为 `true` 时，第 2、3 步顺序对调，即先交给父加载器，与标准的双亲委派一致。

## 运行

环境：JDK 17 或以上、Maven 3。主要依赖：`javax.servlet-api` 4.0.1、dom4j（解析 XML 配置）、Lombok。

程序根据当前工作目录查找 `conf/server.xml` 和 `webapps/`，所以需要在项目根目录下启动。

**方式一：IntelliJ IDEA**

运行 `com.ellen.tomcat.mini.startup.Bootstrap` 的 `main` 方法，工作目录使用项目根目录（默认设置即可）。

**方式二：命令行（macOS / Linux）**

```bash
mvn compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
java -cp "target/classes:$(cat target/classpath.txt)" com.ellen.tomcat.mini.startup.Bootstrap
```

启动后访问以下地址（端口在 `conf/server.xml` 中配置，默认 8080）：


| 地址                                                                                                 | 返回内容            |
| -------------------------------------------------------------------------------------------------- | --------------- |
| [http://localhost:8080/app2/servlet/TestServlet](http://localhost:8080/app2/servlet/TestServlet)   | v2 Test 你好      |


响应头中会带上 `Set-Cookie: jsessionid=...`。控制台会按顺序打印每一层容器、Pipeline、Valve、Filter 的调用，可以对照上面的请求处理流程查看。

## 示例应用

- `app1`、`app2` 两个应用的源码在 `src/main/java/app1`、`src/main/java/app2`，配置和静态文件在 `webapps/app1`、`webapps/app2`。
- 两个应用都有 `TestServlet`、`AuthFilter`、`ContextListener`，类名相同、包名不同，用来演示同一个 Host 下部署多个应用。



## 项目结构

```text
tomcat-mini
├── conf/server.xml          服务器配置
├── webapps/                 Web 应用目录，每个子目录是一个应用
│   ├── app1/                WEB-INF/web.xml、hello.txt
│   └── app2/
└── src/main/java
    ├── com/ellen/tomcat/mini
    │   ├── *.java           核心接口：Container、Pipeline、Valve、Loader 等
    │   ├── startup/         启动入口 Bootstrap
    │   ├── connector/       连接器、HTTP 请求解析和响应、Facade
    │   ├── core/            Host、Context、Wrapper、Pipeline、Filter 链
    │   ├── loader/          自定义类加载器
    │   ├── session/         Session
    │   ├── valves/          Valve 基类、访问日志 Valve
    │   ├── logger/          日志
    │   └── util/            Cookie、URL 解码等工具类
    ├── app1/、app2/         示例应用源码
    └── lib/                 HostListener（Host 级别的 Listener）
```



## 当前的简化与限制

- 只实现了 BIO 模式，NIO（New I/O）尚未实现。
- 每个请求处理完就关闭连接，不复用连接（keep-alive）。
- `conf/server.xml` 只读取 Connector 的 `port` 和 Host 的 `appBase`，其余配置项目前不生效。
- Servlet 的类名直接由 URL 得到，`web.xml` 中的 `servlet-mapping` 目前不生效。
- 响应头在调用 Servlet 之前就已发出，所以 Servlet 中设置的响应头不会被发送；请求的文件不存在时，状态码仍然是 200。
- 示例应用的类也被编译到了 `target/classes`（在类路径上），运行时会先由系统类加载器加载，所以目前看不到自定义类加载器的隔离效果。
- 只监听 `127.0.0.1`，不能从其他机器访问。

