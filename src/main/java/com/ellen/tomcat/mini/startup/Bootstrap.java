package com.ellen.tomcat.mini.startup;


import com.ellen.tomcat.mini.Logger;
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
        Logger logger = new FileLogger();
        container.setLogger(logger);
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
