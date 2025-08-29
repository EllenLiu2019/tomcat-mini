package com.ellen.tomcat.mini.startup;


import com.ellen.tomcat.mini.Loader;
import com.ellen.tomcat.mini.core.*;
import com.ellen.tomcat.mini.connector.http.HttpConnector;
import com.ellen.tomcat.mini.loader.CommonLoader;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;


public class Bootstrap {

    public static final String CLASS_PATH = System.getProperty("user.dir");

    public static String WEB_ROOT = CLASS_PATH;

    public static int PORT = 8080;

    private static int debug = 0;

    public static void main(String[] args) {
        if (debug >= 1) {
            log("startup");
        }

        String file = CLASS_PATH + File.separator + "conf" + File.separator + "server.xml";
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(file);
            Element root = document.getRootElement();
            Element serviceElement = root.element("Service");
            Element connectorelement = serviceElement.element("Connector");
            Attribute portattribute = connectorelement.attribute("port");
            PORT = Integer.parseInt(portattribute.getText());
            Element engine = serviceElement.element("Engine");
            Element hostelement = engine.element("Host");
            Attribute appbaseattribute = hostelement.attribute("appBase");
            WEB_ROOT = WEB_ROOT + File.separator + appbaseattribute.getText();
        } catch (Exception ignored) {
        }

        System.setProperty("com.ellen.tomcat.mini", CLASS_PATH);
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
