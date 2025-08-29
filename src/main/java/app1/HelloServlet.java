package app1;

import javax.servlet.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HelloServlet implements Servlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        String doc = """
                <!DOCTYPE html>\s
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body color="#f0f0f0">
                <h1 align="center">Hello World 你好</h1>
                """;

        response.getWriter().println(doc);
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    @Override
    public void destroy() {

    }
}
