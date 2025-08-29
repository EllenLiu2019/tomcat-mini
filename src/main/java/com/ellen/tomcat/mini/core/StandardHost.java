package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.*;
import com.ellen.tomcat.mini.connector.http.HttpConnector;
import com.ellen.tomcat.mini.loader.CommonLoader;
import com.ellen.tomcat.mini.loader.WebappLoader;
import com.ellen.tomcat.mini.logger.FileLogger;
import lib.HostListener;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class StandardHost extends ContainerBase {

    HttpConnector connector;

    Map<String, StandardContext> contexts = new ConcurrentHashMap<>();//contextName - servletContext

    private final ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();

    private final ArrayList<ContainerListener> listeners = new ArrayList<>();


    public StandardHost() {
        super(new StandardHostValve());
        setName("localhost");
        log("Host created.");
    }

    public String getInfo() {
        return "tomcat mini host, version 0.1";
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        System.out.println("StandardHost invoke()");

        super.invoke(request, response);
    }

    public StandardContext getContext(String docbase) {
        StandardContext context = this.contexts.get(docbase);
        if (context == null) {
            // context 内包含各自的 classLoader & Filter
            context = new StandardContext(docbase, connector);
            ClassLoader parentClassLoader = this.loader.getClassLoader();
            Loader loader = new WebappLoader(docbase, parentClassLoader);
            context.setLoader(loader);
            loader.start();
            context.start();
            this.contexts.put(docbase, context);
        }
        return context;
    }

    public void start() {
        setLogger(new FileLogger());

        addListenerDef(ContainerListenerDef.builder()
                .listenerName("HostListener")
                .listenerClass(HostListener.class.getName())
                .build());

        listenerStart();
        fireContainerEvent("Host Started", this);

        File classPath = new File(System.getProperty("com.ellen.tomcat.mini.webapps"));
        for (String dir : Objects.requireNonNull(classPath.list())) {
            if (!dir.startsWith(".")) {
                getContext(dir);
            }
        }
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public void addContainerListener(ContainerListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public void fireContainerEvent(String type, Object data) {
        if (listeners.isEmpty()) {
            return;
        }

        ContainerEvent event = new ContainerEvent(this, type, data);
        ContainerListener[] list = new ContainerListener[0];
        synchronized (this.listeners) {
            list = this.listeners.toArray(list);
        }
        for (ContainerListener containerListener : list) {
            containerListener.containerEvent(event);
        }

    }

    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (this.listenerDefs) {
            this.listenerDefs.add(listenererDef);
        }
    }

    public void listenerStart() {
        System.out.println("Listener Start..........");

        synchronized (this.listeners) {
            this.listeners.clear();
            for (ContainerListenerDef def : this.listenerDefs) {
                String listenerClass = def.getListenerClass();
                CommonLoader classLoader = (CommonLoader) this.getLoader();
                try {
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                    ContainerListener listener = (ContainerListener) clazz.getConstructor().newInstance();
                    addContainerListener(listener);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
