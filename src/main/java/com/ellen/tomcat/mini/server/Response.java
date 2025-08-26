package com.ellen.tomcat.mini.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Response {

    private static final int BUFFER_SIZE = 1024;

    Request request;

    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fileInputStream = null;
        try {
            File file = new File(HttpServer.WEB_ROOT, request.getUrl());
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                int ch = fileInputStream.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fileInputStream.read(bytes, 0, BUFFER_SIZE);
                }
                output.flush();
            } else {
                String errorMessage = "HTTP/1.1 404 FIle Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 23\r\n" +
                        "\r\n" +
                        "<h1>File Not Found</h1>";
                output.write(errorMessage.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }
}
