package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.*;
import lombok.Getter;
import lombok.Setter;
import com.ellen.tomcat.mini.connector.http.HttpConnector;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 一个 Server 管理多个容器 (Context)，而容器又可以管理多个 Servlet (Wrapper)
public class StandardContext extends ContainerBase implements Context {

    @Getter
    private final String info =
            "tomcat-mini Servlet Context, version 0.1";
    @Setter
    @Getter
    HttpConnector connector;

    @Setter
    @Getter
    ClassLoader loader = StandardContext.class.getClassLoader();

    public StandardContext() {
        super(new StandardContextValve());
        super.log("Container created.");
    }

    // servletName - ServletClassName
    Map<String, String> servletClsMap = new ConcurrentHashMap<>();

    // servletName - servlet
    // Context 中包含了多个 Wrapper
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();

    @Override
    public void invoke(Request request, Response response) throws ServletException, IOException {
        System.out.println("StandardContext invoke");
        super.invoke(request, response);
    }

    /*@Override
    public void invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = ((HttpRequestImpl) request).getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);

        StandardWrapper standardWrapper = servletInstanceMap.get(servletName);
        if (standardWrapper == null) {
            String servletClassName = servletName;
            standardWrapper = new StandardWrapper(servletClassName, this);

            servletClsMap.put(servletName, servletClassName);
            servletInstanceMap.put(servletName, standardWrapper);

        }

        System.out.println("Call service()");

        HttpRequestFacade requestFacade = new HttpRequestFacade(request);
        HttpResponseFacade responseFacade = new HttpResponseFacade(response);
        standardWrapper.invoke(requestFacade, responseFacade);

    }*/

    public Wrapper getWrapper(String name) {
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if (servletWrapper == null) {
            String servletClassName = name;
            servletWrapper = new StandardWrapper(servletClassName, this);
            this.servletClsMap.put(name, servletClassName);
            this.servletInstanceMap.put(name, servletWrapper);
        }
        return servletWrapper;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public String getDocBase() {
        return "";
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }

    @Override
    public String getWrapperClass() {
        return "";
    }

    @Override
    public void setWrapperClass(String wrapperClass) {

    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }

    @Override
    public String findServletMapping(String pattern) {
        return "";
    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void reload() {

    }
}