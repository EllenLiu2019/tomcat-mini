package com.ellen.tomcat.mini.valves;

import com.ellen.tomcat.mini.Container;
import com.ellen.tomcat.mini.Valve;
import lombok.Getter;
import lombok.Setter;

public abstract class ValveBase implements Valve {

    protected static String info = "com.ellen.tomcat.mini.valves.ValveBase/0.1";

    protected Container container;

    @Setter
    @Getter
    protected int debug = 0;

    public String getInfo() {
        return info;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

}