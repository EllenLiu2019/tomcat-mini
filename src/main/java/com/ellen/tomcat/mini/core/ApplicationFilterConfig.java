package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Loader;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

final class ApplicationFilterConfig implements FilterConfig {

    private final StandardContext context;

    private Filter filter;

    private FilterDef filterDef;


    public ApplicationFilterConfig(StandardContext context, FilterDef filterDef) throws Exception {
        this.context = context;
        setFilterDef(filterDef);
    }

    void setFilterDef(FilterDef filterDef) throws Exception {
        this.filterDef = filterDef;
        if (filterDef == null) {
            if (this.filter != null) {
                this.filter.destroy();
            }
            this.filter = null;
        } else {
            initFilter();
        }
    }

    void initFilter() throws Exception {
        if (this.filter != null) {
            return;
        }
        Loader loader = this.context.getLoader(); // app class loader
        Class<?> clazz = loader.getClassLoader().loadClass(this.filterDef.getFilterClass());
        this.filter = (Filter) clazz.getConstructor().newInstance();
        this.filter.init(this);
    }

    Filter getFilter() {
        return this.filter;
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
