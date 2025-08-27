package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Container;
import com.ellen.tomcat.mini.Wrapper;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Wrapper 封装了一个 Servlet 实例
public class StandardWrapper extends ContainerBase implements Wrapper {

    @Getter
    private Servlet servlet;

    @Getter
    @Setter
    private String servletClass;


    public StandardWrapper(String servletClassName, StandardContext parent) {
        this.servletClass = servletClassName;
        this.parent = parent;
        try {
            this.loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public void loadServlet() throws ServletException {
        if (this.servlet != null) {
            return;
        }
        if (this.servletClass == null) {
            throw new ServletException("servlet class has not been specified");
        }

        Servlet servlet;
        Class<?> classClass = null;
        ClassLoader classLoader = this.getLoader();
        try {
            if (classLoader != null) {
                classClass = classLoader.loadClass(this.servletClass);
            }
        } catch (ClassNotFoundException e) {
            throw new ServletException("Servlet class not found");
        }

        try {
            assert classClass != null;
            servlet = (Servlet) classClass.getConstructor().newInstance();
        } catch (Throwable e) {
            throw new ServletException("Failed to instantiate servlet");
        }

        try {
            servlet.init(null);
        } catch (Throwable f) {
            throw new ServletException("Failed initialize servlet.");
        }

        this.servlet = servlet;
    }

    @Override
    public String getInfo() {
        return "tomcat-mini Servlet Context, version 0.1";
    }

    public ClassLoader getLoader() {
        if (this.loader != null) {
            return this.loader;
        }
        return this.parent.getLoader();
    }

    public void invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (this.servlet != null) {
            this.servlet.service(request, response);
        }
    }

    public void addChild(Container child) {
    }

    public Container findChild(String name) {
        return null;
    }

    public Container[] findChildren() {
        return null;
    }

    public void removeChild(Container child) {
    }

    @Override
    public int getLoadOnStartup() {
        return 0;
    }

    @Override
    public void setLoadOnStartup(int value) {

    }

    @Override
    public void addInitParameter(String name, String value) {

    }

    @Override
    public Servlet allocate() throws ServletException {
        return null;
    }

    @Override
    public String findInitParameter(String name) {
        return "";
    }

    @Override
    public String[] findInitParameters() {
        return new String[0];
    }

    @Override
    public void load() throws ServletException {

    }

    @Override
    public void removeInitParameter(String name) {

    }
}