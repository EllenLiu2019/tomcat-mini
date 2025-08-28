package com.ellen.tomcat.mini.startup;


import com.ellen.tomcat.mini.Logger;
import com.ellen.tomcat.mini.core.ContainerListenerDef;
import com.ellen.tomcat.mini.core.FilterDef;
import com.ellen.tomcat.mini.core.FilterMap;
import com.ellen.tomcat.mini.core.StandardContext;
import com.ellen.tomcat.mini.connector.http.HttpConnector;
import com.ellen.tomcat.mini.logger.FileLogger;

public class Bootstrap {

    private static int debug = 0;

    public static void main(String[] args) {
        if (debug >= 1) {
            log(".... startup ....");
        }
        HttpConnector connector = new HttpConnector();
        StandardContext container = new StandardContext();
        connector.setContainer(container);
        container.setConnector(connector);

        container.setLogger(new FileLogger());

        container.addListenerDef(ContainerListenerDef.builder()
                .listenerName("TestListener")
                .listenerClass("server.TestListener")
                .build());

        try {
            container.addFilterDef(FilterDef.builder()
                    .filterName("TestFilter")
                    .filterClass("server.TestFilter")
                    .build());

            container.addFilterMap(FilterMap.builder()
                    .filterName("TestFilter")
                    .urlPattern("/*")
                    .build());
            container.filterBuild();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        connector.start();
    }

    private static void log(String message) {
        System.out.print("Bootstrap: ");
        System.out.println(message);
    }

    private static void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);
    }
}
