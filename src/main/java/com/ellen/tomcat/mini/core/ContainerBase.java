package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Container;
import lombok.Getter;
import lombok.Setter;

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


    public abstract String getInfo();

    public ClassLoader getLoader() {
        if (loader != null)
            return loader;
        if (parent != null)
            return parent.getLoader();
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