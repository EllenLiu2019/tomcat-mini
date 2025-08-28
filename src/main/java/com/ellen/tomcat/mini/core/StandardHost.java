package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.*;
import com.ellen.tomcat.mini.connector.http.HttpConnector;
import com.ellen.tomcat.mini.logger.FileLogger;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardHost extends ContainerBase {

    HttpConnector connector;

    Map<String, StandardContext> contexts = new ConcurrentHashMap<>();//contextName - servletContext

    private ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();

    private ArrayList<ContainerListener> listeners = new ArrayList<>();


    public StandardHost() {
        super(new StandardHostValve());
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
            this.contexts.put(docbase, context);
        }
        return context;
    }

    public void init() {
        fireContainerEvent("Host Started", this);

        setLogger(new FileLogger());

        addListenerDef(ContainerListenerDef.builder()
                .listenerName("TestListener")
                .listenerClass("v1.server.TestListener")
                .build());

        addListenerDef(ContainerListenerDef.builder()
                .listenerName("TestListener")
                .listenerClass("v2.server.TestListener")
                .build());

        listenerStart();
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public void addContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void fireContainerEvent(String type, Object data) {
        if (listeners.isEmpty()) {
            return;
        }

        ContainerEvent event = new ContainerEvent(this, type, data);
        ContainerListener[] list = new ContainerListener[0];
        synchronized (listeners) {
            list = listeners.toArray(list);
        }
        for (ContainerListener containerListener : list) {
            containerListener.containerEvent(event);
        }

    }

    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (listenerDefs) {
            listenerDefs.add(listenererDef);
        }
    }

    public boolean listenerStart() {
        System.out.println("Listener Start..........");
        boolean ok = true;
        synchronized (listeners) {
            listeners.clear();
            for (ContainerListenerDef def : listenerDefs) {
                try {
                    // Identify the class loader we will be using
                    String listenerClass = def.getListenerClass();
                    WebappClassLoader classLoader = (WebappClassLoader) this.getLoader();

                    // Instantiate a new instance of this filter and return it
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                    ContainerListener listener = (ContainerListener) clazz.getConstructor().newInstance();

                    addContainerListener(listener);
                } catch (Throwable t) {
                    t.printStackTrace();
                    ok = false;
                }
            }
        }
        return (ok);
    }
}
