package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class HttpConnector implements Runnable {

    int minProcessors = 3;

    int maxProcessors = 10;

    int curProcessors = 0;

    final Deque<HttpProcessor> processors = new ConcurrentLinkedDeque<>();

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
}
