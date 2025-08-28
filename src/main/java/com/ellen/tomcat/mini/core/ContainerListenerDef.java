package com.ellen.tomcat.mini.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
@Builder
public final class ContainerListenerDef {

    private String description;

    private String displayName;

    private String listenerClass;

    private String listenerName;

    @Builder.Default
    private Map<String,String> parameters = new ConcurrentHashMap<>();


    public Map<String,String> getParameterMap() {
        return this.parameters;
    }

    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("ListenerDef[");
        sb.append("listenerName=");
        sb.append(this.listenerName);
        sb.append(", listenerClass=");
        sb.append(this.listenerClass);
        sb.append("]");
        return (sb.toString());
    }
}