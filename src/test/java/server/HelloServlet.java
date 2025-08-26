package server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HelloServlet implements Servlet {
    @Override
    public void service(Request request, Response response) throws IOException {
        String doc = """
                <!DOCTYPE html>\s
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body color="#f0f0f0">
                <h1 align="center">Hello World 你好</h1>
                """;

        response.getOutput().write(doc.getBytes(StandardCharsets.UTF_8));
    }
}
