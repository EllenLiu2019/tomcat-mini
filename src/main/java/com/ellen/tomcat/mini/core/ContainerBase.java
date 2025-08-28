package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.*;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ContainerBase implements Container {

    protected final Map<String, Container> children = new ConcurrentHashMap<>();

    protected ClassLoader loader;

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected Container parent;

    protected Logger logger;

    @Getter
    protected Pipeline pipeline;

    // 初始化 pipeline, 并设置 pipeline 以及 basic valve 中的 container 为直接父类
    public ContainerBase(Valve basicValve) {
        this.pipeline =  new StandardPipeline(this);
        basicValve.setContainer(this);
        this.pipeline.setBasic(basicValve);
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("ContainerBase invoke");

        this.pipeline.invoke(request, response);
    }

    public synchronized void addValve(Valve valve) {
        pipeline.addValve(valve);
    }

    public Valve getBasic() {
        return pipeline.getBasic();
    }

    public Valve[] getValves() {
        return pipeline.getValves();
    }

    public synchronized void removeValve(Valve valve) {
        pipeline.removeValve(valve);
    }

    public void setBasic(Valve valve) {
        pipeline.setBasic(valve);
    }

    public Logger getLogger() {
        if (this.logger != null) {
            return this.logger;
        }
        if (this.parent != null) {
            return this.parent.getLogger();
        }
        return null;
    }

    public synchronized void setLogger(Logger logger) {
        Logger oldLogger = this.logger;
        if (oldLogger == logger) return;
        this.logger = logger;
    }

    protected void log(String message) {
        Logger logger = this.getLogger();
        if (logger != null) {
            logger.log(logName() + ": " + message);
        } else {
            System.out.println(logName() + ": " + message);
        }
    }

    protected void log(String message, Throwable throwable) {
        Logger logger = this.getLogger();
        if (logger != null) {
            logger.log(logName() + ": " + message, throwable);
        } else {
            System.out.println(logName() + ": " + message + ": " + throwable);
            throwable.printStackTrace(System.out);
        }
    }

    protected String logName() {
        String className = this.getClass().getName();
        int period = className.lastIndexOf(".");
        if (period >= 0) className = className.substring(period + 1);
        return className + "[" + getName() + "]";
    }

    public abstract String getInfo();

    public ClassLoader getLoader() {
        if (loader != null) {
            return loader;
        }
        if (parent != null) {
            return parent.getLoader();
        }
        return null;
    }

    public synchronized void setLoader(ClassLoader loader) {
        ClassLoader oldLoader = this.loader;
        if (oldLoader == loader) {
            return;
        }
        this.loader = loader;
    }

    //-----------下面是对 children map 的增删改查操作-----------------

    public void addChild(Container child) {
        this.addChildInternal(child);
    }

    private void addChildInternal(Container child) {
        synchronized (children) {
            if (children.get(child.getName()) != null) {
                throw new IllegalArgumentException(
                        "addChild:  Child name '" + child.getName() + "' is not unique");
            }
            child.setParent(this);
            children.put(child.getName(), child);
        }
    }

    public Container findChild(String name) {
        if (name == null)
            return (null);
        synchronized (children) {       // Required by post-start changes
            return children.get(name);
        }
    }

    public Container[] findChildren() {
        synchronized (children) {
            Container[] results = new Container[children.size()];
            return children.values().toArray(results);
        }
    }

    public void removeChild(Container child) {
        synchronized (children) {
            if (children.get(child.getName()) == null)
                return;
            children.remove(child.getName());
        }
        child.setParent(null);
    }
}