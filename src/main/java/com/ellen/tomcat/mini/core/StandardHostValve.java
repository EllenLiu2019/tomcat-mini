package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Request;
import com.ellen.tomcat.mini.Response;
import com.ellen.tomcat.mini.ValveContext;
import com.ellen.tomcat.mini.connector.http.HttpRequestImpl;
import com.ellen.tomcat.mini.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

public class StandardHostValve extends ValveBase {
    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        String docbase = ((HttpRequestImpl) request).getDocbase();
        System.out.println("StandardHostValve invoke,  getDocBase : " + docbase);

        StandardHost host = (StandardHost) super.getContainer();
        // 根据 URL 匹配 context
        StandardContext servletContext = host.getContext(docbase);
        try {
            servletContext.invoke(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
