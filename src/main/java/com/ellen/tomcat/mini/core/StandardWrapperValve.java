package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.Request;
import com.ellen.tomcat.mini.Response;
import com.ellen.tomcat.mini.ValveContext;
import com.ellen.tomcat.mini.connector.http.HttpRequestImpl;
import com.ellen.tomcat.mini.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class StandardWrapperValve extends ValveBase {

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        System.out.println("StandardWrapperValve invoke");

        Servlet servlet = ((StandardWrapper) super.getContainer()).getServlet();
        ApplicationFilterChain filterChain = createFilterChain(request, servlet);

        assert filterChain != null;
        filterChain.doFilter((ServletRequest) request, (ServletResponse) response);
        filterChain.release();
    }

    // setup filter chain
    private ApplicationFilterChain createFilterChain(Request request, Servlet servlet) {
        System.out.println("createFilterChain");

        if (servlet == null) {
            return null;
        }

        ApplicationFilterChain filterChain = new ApplicationFilterChain(servlet);
        StandardWrapper wrapper = (StandardWrapper) super.getContainer();
        StandardContext context = (StandardContext) wrapper.getParent();
        FilterMap[] filterMaps = context.findFilterMaps();
        if ((filterMaps == null) || (filterMaps.length == 0)) {
            return filterChain;
        }

        String requestPath = null;
        if (request instanceof HttpServletRequest) {
            requestPath = ((HttpRequestImpl) request).getUri();
        }

        for (FilterMap filterMap : filterMaps) {
            if (matchFiltersURL(filterMap, requestPath)) {
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                        context.findFilterConfig(filterMap.getFilterName());
                if (filterConfig != null) {
                    filterChain.addFilter(filterConfig);
                }
            }
        }

        String servletName = wrapper.getName();
        for (FilterMap filterMap : filterMaps) {
            if (matchFiltersServlet(filterMap, servletName)) {
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                        context.findFilterConfig(filterMap.getFilterName());
                if (filterConfig != null) {
                    filterChain.addFilter(filterConfig);
                }
            }
        }

        return filterChain;

    }

    private boolean matchFiltersURL(FilterMap filterMap, String requestPath) {
        if (requestPath == null) {
            return false;
        }

        String testPath = filterMap.getUrlPattern();
        if (testPath == null) {
            return false;
        }

        if (testPath.equals(requestPath) || testPath.equals("/*")) {
            return true;
        }

        if (testPath.endsWith("/*")) {
            String comparePath = requestPath;
            while (true) {
                if (testPath.equals(comparePath + "/*")) {
                    return true;
                }
                int slash = comparePath.lastIndexOf('/');
                if (slash < 0) {
                    break;
                }
                comparePath = comparePath.substring(0, slash);
            }
            return false;
        }

        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if (slash >= 0 && period > slash) {
                return testPath.equals("*." + requestPath.substring(period + 1));
            }
        }

        return false; // NOTE - Not relevant for selecting filters
    }

    private boolean matchFiltersServlet(FilterMap filterMap, String servletName) {
        if (servletName == null) {
            return false;
        }
        return servletName.equals(filterMap.getServletName());
    }
}
