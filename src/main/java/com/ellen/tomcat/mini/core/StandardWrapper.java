package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.*;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

// Wrapper 封装了一个 Servlet 实例
public class StandardWrapper extends ContainerBase implements Wrapper {

    @Getter
    final String info = "tomcat-mini Servlet Context, version 0.1";

    @Getter
    private Servlet servlet;

    @Getter
    @Setter
    private String servletClass;


    public StandardWrapper(String servletClassName, StandardContext parent) {
        super(new StandardWrapperValve());

        this.servletClass = servletClassName;
        this.parent = parent;
        try {
            this.loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }

        super.log("Wrapper created.");
    }

    public void loadServlet() throws ServletException {
        if (this.servlet != null) {
            return;
        }
        if (this.servletClass == null) {
            throw new ServletException("servlet class has not been specified");
        }

        Servlet servlet = null;
        Class<?> classClass;
        Loader loader = this.getLoader();
        try {
            if (loader != null) {
                classClass = loader.getClassLoader().loadClass(this.servletClass);
                servlet = (Servlet) classClass.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new ServletException("Servlet class not found");
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

    public Loader getLoader() {
        if (this.loader != null) {
            return this.loader;
        }
        return this.parent.getLoader();
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("StandardWrapper invoke()");

        super.invoke(request, response);
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