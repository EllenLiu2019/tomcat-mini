package com.ellen.tomcat.mini.startup;


import com.ellen.tomcat.mini.core.*;
import com.ellen.tomcat.mini.connector.http.HttpConnector;


public class Bootstrap {

    public static final String WEB_ROOT = System.getProperty("user.dir");

    private static int debug = 0;

    public static void main(String[] args) {
        if (debug >= 1) {
            log(".... startup ....");
        }
        System.setProperty("com.ellen.tomcat.mini.base", WEB_ROOT);

        HttpConnector connector = new HttpConnector();
        StandardHost container = new StandardHost();

        WebappClassLoader loader = new WebappClassLoader();
        container.setLoader(loader); // invoke ContainerBase.setLoader()
        loader.init();

        connector.setContainer(container);
        container.setConnector(connector);

        container.init();
        connector.start();
    }

    private static void log(String message) {
        System.out.print("Bootstrap: ");
        System.out.println(message);
    }

}
