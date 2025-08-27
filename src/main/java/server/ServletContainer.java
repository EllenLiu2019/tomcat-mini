package server;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ServletContainer {

    HttpConnector connector;

    ClassLoader loader = ServletContainer.class.getClassLoader();

    // servletName - ServletClassName
    Map<String, String> servletClsMap = new ConcurrentHashMap<>();

    // servletName - servlet
    Map<String, ServletWrapper> servletInstanceMap = new ConcurrentHashMap<>();


    public String getInfo() {
        return null;
    }

    public ClassLoader getLoader() {
        return this.loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public String getName() {
        return null;
    }

    public void setName(String name) {
    }

    //invoke方法用于从map中找到相关的servlet，然后调用
    public void invoke(HttpRequest request, HttpResponse response) throws IOException, ServletException {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);

        ServletWrapper servletWrapper = servletInstanceMap.get(servletName);
        if (servletWrapper == null) {
            String servletClassName = servletName;
            servletWrapper = new ServletWrapper(servletClassName, this);

            servletClsMap.put(servletName, servletClassName);
            servletInstanceMap.put(servletName, servletWrapper);

        }

        System.out.println("Call service()");

        HttpRequestFacade requestFacade = new HttpRequestFacade(request);
        HttpResponseFacade responseFacade = new HttpResponseFacade(response);
        servletWrapper.invoke(requestFacade, responseFacade);

    }
}