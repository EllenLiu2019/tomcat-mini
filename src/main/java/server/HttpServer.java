package server;


public class HttpServer {

    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        ServletContainer container = new ServletContainer();
        connector.setContainer(container);
        container.setConnector(connector);
        connector.start();
    }

}
