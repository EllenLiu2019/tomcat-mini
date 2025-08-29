package com.ellen.tomcat.mini.startup;


import com.ellen.tomcat.mini.Loader;
import com.ellen.tomcat.mini.core.*;
import com.ellen.tomcat.mini.connector.http.HttpConnector;

import java.io.File;


public class Bootstrap {

    public static final String MINI_HOME = System.getProperty("user.dir");

    public static final String WEB_ROOT = MINI_HOME  + File.separator + "webapps";

    public static final int PORT = 8080;

    private static int debug = 1;

    public static void main(String[] args) {
        if (debug >= 1) {
            log("startup");
        }
        System.setProperty("com.ellen.tomcat.mini", MINI_HOME);
        System.setProperty("com.ellen.tomcat.mini.webapps", WEB_ROOT);

        HttpConnector connector = new HttpConnector();
        StandardHost host = new StandardHost();

        Loader loader = new CommonLoader();
        host.setLoader(loader); // invoke ContainerBase.setLoader()
        loader.start();

        connector.setContainer(host);
        host.setConnector(connector);

        host.start(); // host start
        connector.start();
    }

    private static void log(String message) {
        System.out.println("Bootstrap: " + message);
    }

}
