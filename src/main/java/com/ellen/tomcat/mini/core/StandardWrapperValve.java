package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Request;
import com.ellen.tomcat.mini.Response;
import com.ellen.tomcat.mini.ValveContext;
import com.ellen.tomcat.mini.connector.HttpRequestFacade;
import com.ellen.tomcat.mini.connector.HttpResponseFacade;
import com.ellen.tomcat.mini.connector.http.HttpRequestImpl;
import com.ellen.tomcat.mini.connector.http.HttpResponseImpl;
import com.ellen.tomcat.mini.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StandardWrapperValve extends ValveBase {

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        System.out.println("StandardWrapperValve invoke");

        HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
        HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);
        Servlet servlet = ((StandardWrapper) super.getContainer()).getServlet();
        if (servlet != null) {
            servlet.service(requestFacade, responseFacade);
        }
    }
}
