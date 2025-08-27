package com.ellen.tomcat.mini.connector.http;

import com.ellen.tomcat.mini.core.StandardContext;
import com.ellen.tomcat.mini.session.StandardSession;
import lombok.Getter;
import lombok.Setter;

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

public class HttpConnector implements Runnable {

    int minProcessors = 3;

    int maxProcessors = 10;

    int curProcessors = 0;

    final Deque<HttpProcessor> processors = new ConcurrentLinkedDeque<>();

    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private StandardContext container;

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (this.curProcessors < this.minProcessors) {
            HttpProcessor processor = new HttpProcessor(this);
            processor.start();
            processors.push(processor);
            this.curProcessors++;
        }

        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                HttpProcessor processor = createProcessor();

                if (processor == null) {
                    socket.close();
                    continue;
                }

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
        System.out.println("current number of Processors:" + this.curProcessors);
        return processors.pop();
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
}
