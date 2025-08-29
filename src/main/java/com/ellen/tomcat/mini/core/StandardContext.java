package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.*;
import lombok.Getter;
import lombok.Setter;
import com.ellen.tomcat.mini.connector.http.HttpConnector;


import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 一个 Server 管理多个容器 (Context)，而容器又可以管理多个 (Wrapper)， Wrapper 中封装了 Servlet
public class StandardContext extends ContainerBase implements Context {

    @Getter
    private final String info =
            "tomcat-mini Servlet Context, version 0.1";
    @Setter
    @Getter
    HttpConnector connector;

    private ClassLoader loader = StandardContext.class.getClassLoader();

    // servletName - ServletClassName
    Map<String, String> servletClsMap = new ConcurrentHashMap<>();

    // servletName - servlet
    // Context 中包含了多个 Wrapper
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();

    private final Map<String, ApplicationFilterConfig> filterConfigs = new ConcurrentHashMap<>();

    private final Map<String, FilterDef> filterDefs = new ConcurrentHashMap<>();

    private FilterMap[] filterMaps = new FilterMap[0];

    private final ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();

    private final ArrayList<ContainerListener> listeners = new ArrayList<>();

    private String docbase;

    public StandardContext(String docbase, HttpConnector connector) {
        super(new StandardContextValve());
        super.log("Container created.");
        this.docbase = docbase;
        this.connector = connector;
    }

    public void start() {
        addListenerDef(ContainerListenerDef.builder()
                .listenerName("ContextListener")
                .listenerClass(this.docbase + ".server.ContextListener")
                .build());

        listenerStart();
        fireContainerEvent("Container Started", this);

        try {
            addFilterDef(FilterDef.builder()
                    .filterName("AuthFilter")
                    .filterClass(this.docbase + ".server.AuthFilter")
                    .build());

            addFilterMap(FilterMap.builder()
                    .filterName("AuthFilter")
                    .urlPattern("/*")
                    .build());

            filterBuild();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void listenerStart() {
        System.out.println("Context Listener Start..........");

        synchronized (this.listeners) {
            this.listeners.clear();
            for (ContainerListenerDef def : listenerDefs) {
                ContainerListener listener;
                try {
                    String listenerClass = def.getListenerClass();
                    Loader loader = this.getLoader(); // 加载 app class loader

                    Class<?> clazz = loader.getClassLoader().loadClass(listenerClass);
                    listener = (ContainerListener) clazz.getConstructor().newInstance();

                    addContainerListener(listener);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }

    }

    public void addContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
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

    public void addListenerDef(ContainerListenerDef listenerDef) {
        synchronized (listenerDefs) {
            this.listenerDefs.add(listenerDef);
        }
    }

    @Override
    public void invoke(Request request, Response response) throws ServletException, IOException {
        System.out.println("StandardContext invoke");
        super.invoke(request, response);
    }

    public Wrapper getWrapper(String name) {
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if (servletWrapper == null) {
            String servletClassName = name;
            servletWrapper = new StandardWrapper(servletClassName, this);
            this.servletClsMap.put(name, servletClassName);
            this.servletInstanceMap.put(name, servletWrapper);
        }
        return servletWrapper;
    }

    public void addFilterDef(FilterDef filterDef) {
        this.filterDefs.put(filterDef.getFilterName(), filterDef);
    }

    public void addFilterMap(FilterMap filterMap) {
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getUrlPattern();

        if (findFilterDef(filterName) == null) {
            throw new IllegalArgumentException("standardContext.filterMap.name" + filterName);
        }
        if (servletName == null && urlPattern == null) {
            throw new IllegalArgumentException("standardContext.filterMap.either");
        }
        if (servletName != null && urlPattern != null) {
            throw new IllegalArgumentException("standardContext.filterMap.either");
        }

        // 因为过滤器模式是2.3中的新功能，所以不需要调整
        // 对于2.2版本的向后兼容性
        if ((urlPattern != null) && !validateURLPattern(urlPattern)) {
            throw new IllegalArgumentException("standardContext.filterMap.pattern" + urlPattern);
        }

        // 将当前 filterMap 添加到已注册的 filterMaps 中
        synchronized (this.filterMaps) {
            FilterMap[] results = new FilterMap[filterMaps.length + 1];
            System.arraycopy(filterMaps, 0, results, 0, filterMaps.length);
            results[filterMaps.length] = filterMap;
            this.filterMaps = results;
        }
    }

    public FilterDef findFilterDef(String filterName) {
        return filterDefs.get(filterName);
    }

    public FilterMap[] findFilterMaps() {
        return this.filterMaps;
    }

    public void filterBuild() throws Exception {
        System.out.println("Filter Build..........");

        synchronized (this.filterConfigs) {
            this.filterConfigs.clear();
            for (String name : this.filterDefs.keySet()) {
                ApplicationFilterConfig filterConfig = new ApplicationFilterConfig(this, this.filterDefs.get(name));
                this.filterConfigs.put(name, filterConfig);
            }
        }
    }

    public FilterConfig findFilterConfig(String name) {
        return filterConfigs.get(name);
    }

    private boolean validateURLPattern(String urlPattern) {
        if (urlPattern == null) {
            return false;
        }
        if (urlPattern.startsWith("*.")) {
            return urlPattern.indexOf('/') < 0;
        }
        return urlPattern.startsWith("/");
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public String getDocBase() {
        return "";
    }

    @Override
    public void setDocBase(String docBase) {
        this.docbase = docBase;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }

    @Override
    public String getWrapperClass() {
        return "";
    }

    @Override
    public void setWrapperClass(String wrapperClass) {

    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }

    @Override
    public String findServletMapping(String pattern) {
        return "";
    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void reload() {

    }
}