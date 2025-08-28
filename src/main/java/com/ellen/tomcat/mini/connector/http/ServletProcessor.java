package com.ellen.tomcat.mini.connector.http;


import com.ellen.tomcat.mini.Request;
import com.ellen.tomcat.mini.Response;

import javax.servlet.ServletException;
import java.io.*;


public class ServletProcessor {

    private HttpConnector connector;

    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void process(Request request, Response response) throws IOException, ServletException {
        this.connector.getContainer().invoke(request, response);
    }

}
