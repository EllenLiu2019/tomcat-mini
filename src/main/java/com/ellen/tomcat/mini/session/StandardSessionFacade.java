package com.ellen.tomcat.mini.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;

public class StandardSessionFacade implements HttpSession {

    private HttpSession session;

    public StandardSessionFacade(HttpSession session) {
        this.session = session;
    }

    @Override
    public long getCreationTime() {
        return this.session.getCreationTime();
    }

    @Override
    public String getId() {
        return this.session.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return this.session.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return session.getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(int var1) {
        this.session.setMaxInactiveInterval(var1);
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.session.getMaxInactiveInterval();
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return this.session.getSessionContext();
    }

    @Override
    public Object getAttribute(String var1) {
        return this.session.getAttribute(var1);
    }

    @Override
    @Deprecated
    public Object getValue(String var1) {
        return this.session.getValue(var1);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return session.getAttributeNames();
    }

    @Override
    @Deprecated
    public String[] getValueNames() {
        return session.getValueNames();
    }

    @Override
    public void setAttribute(String var1, Object var2) {
        this.session.setAttribute(var1, var2);
    }

    @Override
    @Deprecated
    public void putValue(String var1, Object var2) {
        this.session.putValue(var1, var2);
    }

    @Override
    public void removeAttribute(String var1) {
        session.removeAttribute(var1);
    }

    @Override
    @Deprecated
    public void removeValue(String var1) {
        session.removeAttribute(var1);
    }

    @Override
    public void invalidate() {
        session.invalidate();
    }

    @Override
    public boolean isNew() {
        return session.isNew();
    }
}
