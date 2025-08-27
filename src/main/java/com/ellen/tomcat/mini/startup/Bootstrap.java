package com.ellen.tomcat.mini.startup;


import com.ellen.tomcat.mini.core.StandardContext;
import com.ellen.tomcat.mini.connector.http.HttpConnector;

public class Bootstrap {

    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        StandardContext container = new StandardContext();
        connector.setContainer(container);
        container.setConnector(connector);
        connector.start();
    }

}
