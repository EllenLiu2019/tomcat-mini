package com.ellen.tomcat.mini.loader;

import com.ellen.tomcat.mini.Container;
import com.ellen.tomcat.mini.Loader;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;

// ClassLoader for Application, load class from classpath:webapps/WEB-INF/classes
public class WebappLoader implements Loader {

    ClassLoader classLoader;
    ClassLoader parent;
    String path;
    String docbase;
    Container container;

    public WebappLoader(String docbase) {
        this.docbase = docbase;
    }

    public WebappLoader(String docbase, ClassLoader parent) {
        this.docbase = docbase;
        this.parent = parent;
    }

    // Create a URLClassLoader
    // 加载目录是 com.ellen.tomcat.mini.webapps 规定的根目录，加上应用目录，
    // 然后之下的 /WEB-INF/classes 目录
    // 这意味着每一个应用有自己的类加载器，达到隔离的目的
    @Override
    public void start() {
        System.out.println("Starting WebappLoader");
        try {
            URL[] urls = new URL[1];
            File classPath = new File(System.getProperty("com.ellen.tomcat.mini.webapps"));
            String repository = (new URL(
                    "file",
                    null,
                    classPath.getCanonicalPath() + File.separator))
                    .toString();

            if (docbase != null && !docbase.isEmpty()) {
                repository = repository + docbase + File.separator;
            }
            repository = repository + "WEB-INF" + File.separator + "webapps" + File.separator;
            System.out.println("Webapp classloader Repository : " + repository);

            urls[0] = new URL(null, repository, (URLStreamHandler) null);
            this.classLoader = new WebappClassLoader(urls, parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Container getContainer() {
        return this.container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getDocbase() {
        return this.docbase;
    }

    @Override
    public void setDocbase(String docbase) {
        this.docbase = docbase;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public String getInfo() {
        return "A simple loader";
    }

    @Override
    public void addRepository(String repository) {
    }

    @Override
    public String[] findRepositories() {
        return new String[0];
    }

    @Override
    public void stop() {

    }
}
