package com.ellen.tomcat.mini;

import lombok.Getter;
import lombok.ToString;

import java.util.EventObject;

@Getter
@ToString
public final class ContainerEvent extends EventObject {

    private final Container container;

    private final String type;

    private final Object data;

    public ContainerEvent(Container container, String type, Object data) {
        super(container);
        this.container = container;
        this.type = type;
        this.data = data;
    }

}
