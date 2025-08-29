package com.ellen.tomcat.mini.connector.http;

import com.ellen.tomcat.mini.*;
import com.ellen.tomcat.mini.session.StandardSession;
import lombok.Getter;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.ellen.tomcat.mini.startup.Bootstrap.PORT;

public class HttpConnector implements Connector, Runnable {

    private String info = "com.ellen.tomcat.mini.connector.http.HttpConnector/0.1";

    int minProcessors = 3;

    int maxProcessors = 10;

    int curProcessors = 0;

    final Deque<HttpProcessor> processors = new ConcurrentLinkedDeque<>();

    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    @Getter
    private Container container;

    private String threadName;

    public void start() {
        this.threadName = "HttpConnector[" + PORT + "]";
        log("httpConnector.starting  " + threadName);
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        while (this.curProcessors < this.minProcessors) {
            HttpProcessor processor = new HttpProcessor(this);
            processor.start();
            processors.push(processor);
            this.curProcessors++;
        }

        ServerSocket serverSocket;
        try {
            // BIO: 服务器启动 ServerSocket
            // TODO: NIO: 服务器启动 ServerSocketChannel；Channel 设置成非阻塞式；注册 selector
            serverSocket = new ServerSocket(PORT, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 无限循环 + wait + notify
        while (true) {
            try {
                // BIO：在 PORT 端口监听 Socket 请求，
                // accept 阻塞等待 网络请求
                Socket socket = serverSocket.accept();
                HttpProcessor processor = createProcessor();

                if (processor == null) {
                    socket.close();
                    continue;
                }

                // BIO：每个请求分配一个线程处理
                processor.assign(socket);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private HttpProcessor createProcessor() {
        synchronized (this.processors) {
            if (!processors.isEmpty()) {
                return processors.pop();
            }
            if (this.curProcessors < this.maxProcessors) {
                return doCreateProcessor();
            } else {
                return null;
            }
        }
    }

    private HttpProcessor doCreateProcessor() {
        HttpProcessor processor = new HttpProcessor(this);
        processor.start();
        this.processors.push(processor);
        this.curProcessors++;
        log("current number of Processors:" + this.curProcessors);
        return processors.pop();
    }

    private void log(String message) {
        Logger logger = this.container.getLogger();
        String localName = this.threadName;
        if (localName == null) {
            localName = "HttpConnector";
        }
        if (logger != null) {
            logger.log(localName + " " + message);
        } else {
            System.out.println(localName + " " + message);
        }
    }

    public void recycle(HttpProcessor processor) {
        processors.push(processor);
    }

    public static StandardSession createSession() {
        StandardSession standardSession = new StandardSession();
        standardSession.setValid(true);
        standardSession.setCreationTime(System.currentTimeMillis());

        String sessionId = generateSessionId();
        standardSession.setId(sessionId);

        sessions.put(sessionId, standardSession);
        return standardSession;
    }

    protected static synchronized String generateSessionId() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        byte[] bytes = new byte[16];
        random.nextBytes(bytes);

        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            byte b1 = (byte) ((aByte & 0xf0) >> 4);
            byte b2 = (byte) (aByte & 0x0f);

            if (b1 < 10) {
                result.append((char) ('0' + b1));
            } else {
                result.append((char) ('A' + (b1 - 10)));
            }

            if (b2 < 10) {
                result.append((char) ('0' + b2));
            } else {
                result.append((char) ('A' + (b2 - 10)));
            }
        }

        return result.toString();
    }


    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public String getInfo() {
        return "";
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public Request createRequest() {
        return null;
    }

    @Override
    public Response createResponse() {
        return null;
    }

    @Override
    public void initialize() {

    }
}
