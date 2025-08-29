package com.ellen.tomcat.mini.loader;

import com.ellen.tomcat.mini.Container;
import com.ellen.tomcat.mini.Loader;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;

// 全局类加载器，加载目录： /lib
public class CommonLoader implements Loader {

    ClassLoader classLoader;
    ClassLoader parent;
    String path;
    String docbase;
    Container container;


    public CommonLoader() {
    }

    public CommonLoader(ClassLoader parent) {
        this.parent = parent;
    }

    @Override
    public synchronized void start() {
        System.out.println("Starting Common Loader, docbase: " + docbase);

        try {
            URL[] urls = new URL[1];
            File classPath = new File(System.getProperty("com.ellen.tomcat.mini"));
            String repository = (new URL(
                    "file",
                    null,
                    classPath.getCanonicalPath() + File.separator))
                    .toString();
            //repository = repository + "lib" + File.separator;

            urls[0] = new URL(null, repository, (URLStreamHandler) null);

            System.out.println("Common classloader Repository : " + repository);

            this.classLoader = new CommonClassLoader(urls);
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
        return this.classLoader;
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
