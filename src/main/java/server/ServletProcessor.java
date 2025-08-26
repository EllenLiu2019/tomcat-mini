package server;


import javax.servlet.Servlet;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;


public class ServletProcessor {

    private final static String OK_Message = """
            HTTP/1.1 ${StatusCode} ${StatusName}\r
            Content-Type: ${ContentType}\r
            Server: mini\r
            Date: ${ZonedDateTime}\r
            \r
            """;

    public void process(HttpRequest request, HttpResponse response) throws IOException {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader;

        try {
            response.setCharacterEncoding("UTF-8");
            response.sendHeaders();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Class<?> servletClass;
        try {
            servletClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Servlet servlet;
        try {
            servlet = (Servlet) servletClass.getConstructor().newInstance();
            servlet.service(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

}
