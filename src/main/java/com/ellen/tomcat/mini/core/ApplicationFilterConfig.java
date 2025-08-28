package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Context;
import lombok.Getter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

final class ApplicationFilterConfig implements FilterConfig {

    private StandardContext context;

    @Getter
    private Filter filter;
    private FilterDef filterDef;


    public ApplicationFilterConfig(StandardContext context, FilterDef filterDef)
            throws ClassCastException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, ServletException, InvocationTargetException, NoSuchMethodException {
        this.context = context;
        setFilterDef(filterDef);
    }

    void setFilterDef(FilterDef filterDef)
            throws ClassCastException, ClassNotFoundException,
            IllegalAccessException, InstantiationException,
            ServletException, InvocationTargetException, NoSuchMethodException {
        this.filterDef = filterDef;
        if (filterDef == null) {
            // 释放之前分配的所有过滤器实例
            if (this.filter != null) {
                this.filter.destroy();
            }
            this.filter = null;
        } else {
            // 分配一个新的过滤器实例
            initFilter();
        }
    }

    void initFilter() throws ClassCastException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, ServletException, NoSuchMethodException, InvocationTargetException {

        if (this.filter != null) {
            return;
        }

        ClassLoader classLoader = context.getLoader();
        Class<?> clazz = classLoader.loadClass(filterDef.getFilterClass());
        this.filter = (Filter) clazz.getConstructor().newInstance();
        filter.init(this);
    }

    public String getFilterName() {
        return filterDef.getFilterName();
    }

    public String getInitParameter(String name) {
        Map<String, String> map = filterDef.getParameters();
        if (map == null) {
            return null;
        }
        return map.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        Map<String, String> map = filterDef.getParameters();
        if (map == null) {
            return Collections.enumeration(new ArrayList<>());
        }
        return (Collections.enumeration(map.keySet()));
    }

    public ServletContext getServletContext() {
        return (this.context.getServletContext());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("ApplicationFilterConfig[");
        sb.append("name=");
        sb.append(filterDef.getFilterName());
        sb.append(", filterClass=");
        sb.append(filterDef.getFilterClass());
        sb.append("]");
        return (sb.toString());
    }

    FilterDef getFilterDef() {
        return this.filterDef;
    }

    void release() {
        if (this.filter != null) {
            filter.destroy();
        }
        this.filter = null;
    }
}
