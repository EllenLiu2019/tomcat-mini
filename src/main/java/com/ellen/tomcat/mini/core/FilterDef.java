package com.ellen.tomcat.mini.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@Setter
@Builder
public final class FilterDef {

    private String description = null;

    private String displayName = null;

    private String filterClass;

    private String filterName;

    private String largeIcon;

    private Map<String,String> parameters = new ConcurrentHashMap<>();

    private String smallIcon = null;

    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("FilterDef[");
        sb.append("filterName=");
        sb.append(this.filterName);
        sb.append(", filterClass=");
        sb.append(this.filterClass);
        sb.append("]");
        return sb.toString();
    }
}