package server;


import javax.servlet.Servlet;
import java.io.*;


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

        try {
            response.setCharacterEncoding("UTF-8");
            response.sendHeaders();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            Class<?> servletClass = this.getClass().getClassLoader().loadClass(servletName);
            Servlet servlet = (Servlet) servletClass.getConstructor().newInstance();
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            servlet.service(requestFacade, responseFacade);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
