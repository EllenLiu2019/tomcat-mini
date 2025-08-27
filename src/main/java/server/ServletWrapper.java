package server;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletWrapper {

    private Servlet instance;

    @Getter
    @Setter
    private String servletClass;

    private ClassLoader loader;

    private String name;

    @Getter
    @Setter
    protected ServletContainer parent;


    public ServletWrapper(String servletClassName, ServletContainer parent) {
        this.servletClass = servletClassName;
        this.parent = parent;
        try {
            this.loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public void loadServlet() throws ServletException {
        if (this.instance != null) {
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

        this.instance = servlet;
    }

    public ClassLoader getLoader() {
        if (this.loader != null) {
            return this.loader;
        }
        return this.parent.getLoader();
    }

    public Servlet getServlet() {
        return this.instance;
    }

    public void invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (this.instance != null) {
            this.instance.service(request, response);
        }
    }
}