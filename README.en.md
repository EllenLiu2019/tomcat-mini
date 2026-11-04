# tomcat-mini

<p align="center">English | <a href="README.md">中文简体</a></p>

A simplified Servlet container, hand-written in Java 17 with reference to the source code structure of Tomcat. It is used to study the core design of Tomcat: the connector and its processor pool, the Host / Context / Wrapper container hierarchy, the Pipeline / Valve chain of responsibility, filters and listeners, sessions, and custom class loaders.

## Features

- **Connector**: Uses BIO (Blocking I/O). A `ServerSocket` listens on the port. At startup, 3 `HttpProcessor` threads are created, and more are added when needed, up to 10. If no thread is idle and the limit has been reached, the new connection is closed. The connector thread passes each socket to a processor thread through `wait()` / `notifyAll()`.
- **HTTP parsing**: Parses the request line, headers, and cookies. When a servlet is called, it receives facade objects (`HttpRequestFacade` / `HttpResponseFacade`), so it cannot call the internal methods of the container.
- **Session**: The session ID is passed in a cookie (`jsessionid`) or in the URL as `;jsessionid=`. Sessions are stored in memory.
- **Container hierarchy**: `StandardHost` → `StandardContext` → `StandardWrapper`. At startup, each subdirectory under `webapps/` corresponds to one Context (one web application). Each Wrapper wraps one servlet instance.
- **Pipeline / Valve (chain of responsibility)**: Each container level has a Pipeline. The Pipeline calls its Valves in order and calls the Basic Valve last. The Basic Valve finds the container at the next level and calls it.
- **Filter chain**: For each request, an `ApplicationFilterChain` is created based on the `filter-mapping` entries in `web.xml`. It runs the filters in order and then calls the servlet.
- **Listeners**: When the Host starts, it publishes a container event, which is received by `HostListener`. The listeners of each application are registered based on its `web.xml`.
- **Configuration files**: `conf/server.xml` is read at startup. When each application starts, it reads its own `WEB-INF/web.xml` (`listener`, `filter`, `filter-mapping`, `servlet`). Servlets with `load-on-startup` are created when the application starts; other servlets are created on the first request.
- **Custom class loaders**: `CommonClassLoader` (shared by all applications) and `WebappClassLoader` (one for each application). The loading order is different from the standard parent delegation model. See [Class Loading](#class-loading).
- **Static resources**: For requests that are not for a servlet, the file in the application directory is returned based on the path.
- **Logging**: `FileLogger` writes container logs to the `logs/` directory, with one file for each day.

## Request Processing Flow

```text
HttpConnector                 Listens on the port and passes each socket to an idle HttpProcessor
└─ HttpProcessor              Parses the request. If the path after the application name starts with /servlet/,
                              passes the request to the container; otherwise, handles it as a static file
   └─ StandardHost            Pipeline → StandardHostValve: finds the Context by the first URL segment (e.g. app1)
      └─ StandardContext      Pipeline → StandardContextValve: finds the Wrapper by the last URL segment
         └─ StandardWrapper   Pipeline → StandardWrapperValve: creates the filter chain
            └─ Filter → ... → Servlet.service()
```

## URL Rules

- `/<app-name>/servlet/<ClassName>`: calls the servlet `<app-name>.<ClassName>`. For example, `/app1/servlet/TestServlet` corresponds to `app1.TestServlet`.
- Other paths: return the file under `webapps/<app-name>/`. For example, `/app1/hello.txt` corresponds to `webapps/app1/hello.txt`.

## Class Loading

```text
System class loader (application class path)
└─ CommonClassLoader          Shared by all applications
   ├─ WebappClassLoader       app1
   └─ WebappClassLoader       app2
```

The lookup order of `loadClass()` (`delegate` is `false` by default):

1. Ask the system class loader first, so that classes in an application cannot replace JDK classes.
2. Search its own directory.
3. If the class is still not found, ask the parent class loader.

When `delegate` is `true`, steps 2 and 3 are swapped, which means the parent class loader is asked first. This is the same as the standard parent delegation model.

## Running

Requirements: JDK 17 or later, and Maven 3. Main dependencies: `javax.servlet-api` 4.0.1, dom4j (for parsing XML configuration), and Lombok.

The program finds `conf/server.xml` and `webapps/` based on the current working directory, so it must be started from the project root directory.

**Option 1: IntelliJ IDEA**

Run the `main` method of `com.ellen.tomcat.mini.startup.Bootstrap`, with the project root directory as the working directory (this is the default setting).

**Option 2: Command line (macOS / Linux)**

```bash
mvn compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
java -cp "target/classes:$(cat target/classpath.txt)" com.ellen.tomcat.mini.startup.Bootstrap
```

After startup, open the following URL (the port is configured in `conf/server.xml`, and the default is 8080):

| URL | Response |
| --- | --- |
| [http://localhost:8080/app2/servlet/TestServlet](http://localhost:8080/app2/servlet/TestServlet) | v2 Test 你好 |

The response headers include `Set-Cookie: jsessionid=...`. The console prints the calls of each container level, Pipeline, Valve, and Filter in order. You can compare this output with the request processing flow above.

## Sample Applications

- The source code of the two applications, `app1` and `app2`, is in `src/main/java/app1` and `src/main/java/app2`. Their configuration and static files are in `webapps/app1` and `webapps/app2`.
- Both applications contain `TestServlet`, `AuthFilter`, and `ContextListener`. The class names are the same, but the packages are different. They are used to demonstrate how multiple applications are deployed under one Host.

## Project Structure

```text
tomcat-mini
├── conf/server.xml          Server configuration
├── webapps/                 Web application directory; each subdirectory is one application
│   ├── app1/                WEB-INF/web.xml, hello.txt
│   └── app2/
└── src/main/java
    ├── com/ellen/tomcat/mini
    │   ├── *.java           Core interfaces: Container, Pipeline, Valve, Loader, etc.
    │   ├── startup/         Entry point: Bootstrap
    │   ├── connector/       Connector, HTTP request parsing and response, facades
    │   ├── core/            Host, Context, Wrapper, Pipeline, filter chain
    │   ├── loader/          Custom class loaders
    │   ├── session/         Session
    │   ├── valves/          Valve base class, access log Valve
    │   ├── logger/          Logging
    │   └── util/            Utilities for cookies, URL decoding, etc.
    ├── app1/, app2/         Source code of the sample applications
    └── lib/                 HostListener (a Host-level listener)
```

## Current Simplifications and Limitations

- Only the BIO mode is implemented. NIO (New I/O) is not implemented yet.
- The connection is closed after each request, so connections are not reused (no keep-alive).
- From `conf/server.xml`, only the `port` of the Connector and the `appBase` of the Host are read. Other settings have no effect at present.
- The servlet class name is taken directly from the URL. `servlet-mapping` in `web.xml` has no effect at present.
- Response headers are sent before the servlet is called, so headers set in a servlet are not sent. When a requested file does not exist, the status code is still 200.
- The classes of the sample applications are also compiled into `target/classes` (which is on the class path). At runtime, they are loaded by the system class loader first, so the isolation provided by the custom class loaders cannot be observed at present.
- The server listens only on `127.0.0.1`, so it cannot be accessed from other machines.
