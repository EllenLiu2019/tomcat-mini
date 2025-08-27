package server;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {

    private Socket socket;

    private volatile boolean available = false;

    private final HttpConnector connector;

    private int serverPort = 0;

    private boolean keepAlive = false;

    private boolean http11 = true;


    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public synchronized void assign(Socket socket) {
        while (this.available) {
            try {
                wait();
            } catch (InterruptedException ignore) {
            }
        }

        // connector assign socket when available=false
        this.socket = socket;
        this.available = true;
        notifyAll();
    }

    @Override
    public void run() {
        while (true) {
            Socket socket = await();
            if (socket == null) {
                continue;
            }
            this.process(socket);
            this.connector.recycle(this);
        }
    }

    private synchronized Socket await() {
        while (!this.available) {
            try {
                wait();
            } catch (InterruptedException ignore) {
            }
        }

        // processor working when available=true
        Socket socket = this.socket;
        this.available = false;
        notifyAll();
        return socket;
    }

    private void process(Socket socket) {

        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            this.keepAlive = true;

            while (this.keepAlive) {
                HttpRequest request = new HttpRequest(inputStream);
                request.parse(socket);

                if (request.getSessionId() == null || request.getSession() == null) {
                    request.getSession(true);
                }

                HttpResponse response = new HttpResponse(outputStream);

                response.setRequest(request);
                request.setResponse(response);

                try {
                    response.sendHeaders();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                if (request.getUri().startsWith("/servlet/")) {
                    ServletProcessor processor = new ServletProcessor(this.connector);
                    processor.process(request, response);
                } else {
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request, response);
                }

                response.finishResponse();

                String connection = response.getHeader("Connection");
                if ("close".equals(connection)) {
                    this.keepAlive = false;
                }
            }

            socket.close();
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private void finishResponse(HttpResponse response) {
        response.finishResponse();
    }
}
