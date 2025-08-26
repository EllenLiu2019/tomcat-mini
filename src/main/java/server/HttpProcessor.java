package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {

    private Socket socket;

    private volatile boolean available = false;

    private HttpConnector connector;


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
            if (socket == null) continue;
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
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            Request request = new Request(inputStream);
            request.parse();

            Response response = new Response(outputStream);
            response.setRequest(request);

            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
