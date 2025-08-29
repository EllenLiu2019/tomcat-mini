package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.connector.HttpRequestFacade;
import com.ellen.tomcat.mini.connector.HttpResponseFacade;
import com.ellen.tomcat.mini.connector.http.HttpRequestImpl;
import com.ellen.tomcat.mini.connector.http.HttpResponseImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ApplicationFilterChain implements FilterChain {


    private ArrayList<ApplicationFilterConfig> filters = new ArrayList<>();

    private Iterator<ApplicationFilterConfig> iterator;

    private Servlet servlet;

    public ApplicationFilterChain(Servlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        System.out.println("FilterChain");

        internalDoFilter(request, response);
    }

    private void internalDoFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (this.iterator == null) {
            this.iterator = filters.iterator();
        }

        // 从 Filters 中依次取出 filter, invoke filer.doFilter()，执行每个 filter 中的 逻辑
        if (this.iterator.hasNext()) {
            ApplicationFilterConfig filterConfig = iterator.next();
            Filter filter;
            try {
                filter = filterConfig.getFilter();

                System.out.println("Filter doFilter");
                filter.doFilter(request, response, this);
            } catch (IOException | ServletException | RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new ServletException("filterChain.filter", e);
            }
            return;
        }

        // Fell off the end of chain, call servlet.service
        try {
            HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
            HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);

            this.servlet.service(requestFacade, responseFacade);
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException("filterChain.servlet", e);
        }

    }

    void addFilter(ApplicationFilterConfig filterConfig) {
        this.filters.add(filterConfig);
    }

    void release() {
        this.filters.clear();
        this.servlet = null;
    }

}
