package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Request;
import com.ellen.tomcat.mini.Response;
import com.ellen.tomcat.mini.ValveContext;
import com.ellen.tomcat.mini.connector.http.HttpRequestImpl;
import com.ellen.tomcat.mini.valves.ValveBase;
import lombok.Getter;

import javax.servlet.ServletException;
import java.io.IOException;

final class StandardContextValve extends ValveBase {

    @Getter
    private final String info =
            "org.apache.catalina.core.StandardContextValve/1.0";

    public void invoke(Request request, Response response, ValveContext valveContext) throws IOException, ServletException {
        System.out.println("StandardContextValve invoke()");

        String uri = ((HttpRequestImpl) request).getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        StandardContext context = (StandardContext) super.getContainer();
        StandardWrapper servletWrapper = (StandardWrapper) context.getWrapper(servletName);

        System.out.println("Call service()");
        servletWrapper.invoke(request, response);

    }
}
