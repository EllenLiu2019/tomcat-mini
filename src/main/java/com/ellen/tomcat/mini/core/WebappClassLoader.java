package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Container;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class WebappClassLoader extends ClassLoader {

    ClassLoader classLoader;
    String path;
    String docbase;
    Container container;

    public WebappClassLoader() {
        this.init();
    }
    public synchronized void init() {
        System.out.println("Starting WebappLoader");

        try {
            URL[] urls = new URL[1];
            File classPath = new File(System.getProperty("com.ellen.tomcat.mini.base"));
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            if (docbase != null && !docbase.isEmpty()) {
                repository = repository + docbase + File.separator;
                System.out.println("Webapp classloader Repository : " + repository);
            }

            urls[0] = new URL(null, repository, (URLStreamHandler) null);
            classLoader = new URLClassLoader(urls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocbase() {
        return docbase;
    }

    public void setDocbase(String docbase) {
        this.docbase = docbase;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getInfo() {
        return "A simple loader";
    }

    public void addRepository(String repository) {
    }

    public String[] findRepositories() {
        return null;
    }

    public void stop() {
    }
}