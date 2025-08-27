package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Context;
import com.ellen.tomcat.mini.Wrapper;
import lombok.Getter;
import lombok.Setter;
import com.ellen.tomcat.mini.connector.http.HttpConnector;
import com.ellen.tomcat.mini.connector.HttpRequestFacade;
import com.ellen.tomcat.mini.connector.http.HttpRequestImpl;
import com.ellen.tomcat.mini.connector.HttpResponseFacade;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 一个 Server 管理多个容器 (Context)，而容器又可以管理多个 Servlet (Wrapper)
public class StandardContext extends ContainerBase implements Context {

    @Setter
    @Getter
    HttpConnector connector;

    @Setter
    @Getter
    ClassLoader loader = StandardContext.class.getClassLoader();

    // servletName - ServletClassName
    Map<String, String> servletClsMap = new ConcurrentHashMap<>();

    // servletName - servlet
    // Context 中包含了多个 Wrapper
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();


    public String getInfo() {
        return "tomcat-mini Servlet Context, version 0.1";
    }

    @Override
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