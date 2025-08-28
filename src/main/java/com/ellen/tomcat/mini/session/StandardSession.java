package com.ellen.tomcat.mini.session;

import com.ellen.tomcat.mini.Session;
import com.ellen.tomcat.mini.SessionEvent;
import com.ellen.tomcat.mini.SessionListener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardSession implements HttpSession, Session {

    private String sessionId;

    private long creationTime;

    private boolean valid;

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    private final transient ArrayList<SessionListener> listeners = new ArrayList<>();

    public void addSessionListener(SessionListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeSessionListener(SessionListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
    }

    public void setId(String sessionId) {
        this.sessionId = sessionId;
        fireSessionEvent(Session.SESSION_CREATED_EVENT, null);
    }

    public void fireSessionEvent(String type, Object data) {
        if (this.listeners.isEmpty()) {
            return;
        }

        SessionEvent event = new SessionEvent(this, type, data);
        SessionListener[] list = new SessionListener[0];
        synchronized (listeners) {
            list = this.listeners.toArray(list);
        }
        for (SessionListener sessionListener : list) {
            sessionListener.sessionEvent(event);
        }
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public String getId() {
        return this.sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {

    }

    @Override
    public void setNew(boolean isNew) {

    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    @Deprecated
    public Object getValue(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String var1, Object var2) {
        this.attributes.put(var1, var2);
    }

    @Override
    public void putValue(String var1, Object var2) {
        this.attributes.put(var1, var2);
    }

    @Override
    public void removeAttribute(String var1) {
        this.attributes.remove(var1);
    }

    @Override
    public void removeValue(String s) {

    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public void setValid(boolean b) {
        this.valid = b;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void access() {

    }

    @Override
    public void expire() {

    }

    @Override
    public void recycle() {

    }

    public void setCreationTime(long currentTimeMillis) {
        this.creationTime = currentTimeMillis;
    }

    @Override
    public String getInfo() {
        return "";
    }
}
