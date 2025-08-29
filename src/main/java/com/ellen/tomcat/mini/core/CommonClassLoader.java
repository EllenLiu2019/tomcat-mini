package com.ellen.tomcat.mini.core;

import java.net.URL;
import java.net.URLClassLoader;

public class CommonClassLoader extends URLClassLoader {

    protected boolean delegate = false;

    private ClassLoader parent;

    private ClassLoader system;

    public CommonClassLoader() {
        this(new URL[0]);
    }

    public CommonClassLoader(URL[] urls) {
        super(urls);
        this.parent = super.getParent();
        this.system = super.getSystemClassLoader();
    }

    public CommonClassLoader(ClassLoader parent) {
        this(new URL[0], parent);
    }

    public CommonClassLoader(URL[] urls, ClassLoader parent) {
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

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz;

        // 尝试使用系统类加载器加载类，防止Web应用程序 覆盖J2SE类
        try {
            clazz = this.system.loadClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        boolean delegateLoad = delegate;

        // (1) Delegate to parent if requested
        if (delegateLoad) {
            clazz = this.delegateParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        // (2) Search local repositories
        try {
            clazz = this.findClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException ignored) {
        }

        // (3) Delegate to parent unconditionally
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

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        clazz = super.findClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }

    private void log(String message) {
        System.out.println("WebappClassLoader: " + message);
    }

    private void log(String message, Throwable throwable) {
        System.out.println("WebappClassLoader: " + message);
        throwable.printStackTrace(System.out);
    }
}
