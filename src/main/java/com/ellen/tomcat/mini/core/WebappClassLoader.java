package com.ellen.tomcat.mini.core;


import java.net.URL;
import java.net.URLClassLoader;

// ClassLoader for context
public class WebappClassLoader extends URLClassLoader {

    protected boolean delegate = false;
    private ClassLoader parent; // CommonClassLoader
    private ClassLoader system; // Java inner ClassLoader

    public WebappClassLoader() {
        this(new URL[0]);
    }

    public WebappClassLoader(URL[] urls) {
        super(urls);
        this.parent = super.getParent();
        this.system = super.getSystemClassLoader();
    }

    public WebappClassLoader(ClassLoader parent) {
        this(new URL[0], parent);
    }

    public WebappClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
        this.system = super.getSystemClassLoader();
    }

    public boolean getDelegate() {
        return this.delegate;
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = super.findClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz;

        // 系统类加载器，防止覆盖 Java 系统类
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException ignored) {
        }

        boolean delegateLoad = delegate;
        if (delegateLoad) {
            clazz = this.delegateParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException ignored) {
        }

        if (!delegateLoad) {
            clazz = this.delegateParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }
        throw new ClassNotFoundException(name);
    }

    private Class<?> delegateParent(String name, boolean resolve) {
        Class<?> clazz;
        ClassLoader loader = parent;

        if (loader == null) {
            loader = system;
        }

        try {
            clazz = loader.loadClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    public String getInfo() {
        return "A simple loader";
    }

}