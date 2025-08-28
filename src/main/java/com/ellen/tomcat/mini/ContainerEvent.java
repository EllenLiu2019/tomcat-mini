package com.ellen.tomcat.mini;

import lombok.Getter;

import java.util.EventObject;

@Getter
public final class ContainerEvent extends EventObject {

    private final Container container;
    private final Object data;
    private final String type;

    public ContainerEvent(Container container, String type, Object data) {
        super(container);
        this.container = container;
        this.type = type;
        this.data = data;
    }

    public String toString() {
        return ("ContainerEvent['" + getContainer() + "','" +
                getType() + "','" + getData() + "']");
    }
}
