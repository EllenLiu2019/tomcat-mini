package com.ellen.tomcat.mini.connector.http;

import com.ellen.tomcat.mini.*;
import com.ellen.tomcat.mini.session.StandardSessionFacade;
import lombok.Getter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequestImpl implements HttpServletRequest, Request {

    protected HashMap<String, String> headers = new HashMap<>();

    protected Map<String, String[]> parameters = new ConcurrentHashMap<>();

    private InputStream inputStream;

    SocketInputStream sis;

    HttpRequestLine requestLine = new HttpRequestLine();

    @Getter
    private String uri;

    private int port;

    private InetAddress address;

    private String queryString;

    private boolean parsed = false;

    Cookie[] cookies;

    HttpSession session;

    String sessionId;

    StandardSessionFacade standardSessionFacade;

    private HttpResponseImpl response;

    public HttpRequestImpl() {
    }

    public HttpRequestImpl(InputStream inputStream) {
        this.inputStream = inputStream;
        this.sis = new SocketInputStream(this.inputStream, 2 * 1024);
    }

    public void setStream(InputStream input) {
        this.inputStream = input;
        this.sis = new SocketInputStream(this.inputStream, 2 * 1024);
    }

    public void parse(Socket socket) {
        try {
            this.parseConnection(socket);

            this.sis.readRequestLine(requestLine);
            this.parseRequestLine();

            this.parseHeaders();
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }

    }

    private void parseConnection(Socket socket) {
        this.address = socket.getInetAddress();
        this.port = socket.getPort();
    }

    private void parseRequestLine() {

        String tmp = ";" + DefaultHeaders.JSESSIONID_NAME + "=";

        int question = requestLine.indexOf("?");

        if (question >= 0) {
            this.queryString = new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1);
            this.uri = new String(requestLine.uri, 0, question);

            int semicolon = uri.indexOf(tmp);
            if (semicolon > 0) {
                this.sessionId = uri.substring(semicolon + tmp.length());
                this.uri = uri.substring(0, semicolon);
            }
        } else {
            this.queryString = null;
            this.uri = new String(requestLine.uri, 0, requestLine.uriEnd);

            int semicolon = uri.indexOf(tmp);
            if (semicolon >= 0) {
                this.sessionId = uri.substring(semicolon + tmp.length());
                this.uri = uri.substring(0, semicolon);
            }
        }
    }

    protected void parseParameters() {
        //设置字符集
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        //获取查询串， For Both GET & POST
        String qString = getQueryString();
        if (qString != null) {
            byte[] bytes;
            try {
                bytes = qString.getBytes(encoding);
                parseParameters(this.parameters, bytes, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //获取 content Type
        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }

        // POST方法，从 body 中解析参数
        if ("POST".equals(getMethod()) && (getContentLength() > 0)
                && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength();
                int len = 0;
                byte[] buf = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0) {
                        break;
                    }
                    len += next;
                }
                is.close();
                if (len < max) {
                    throw new RuntimeException("Content length mismatch");
                }
                parseParameters(this.parameters, buf, encoding);
            } catch (UnsupportedEncodingException ignored) {
            } catch (IOException e) {
                throw new RuntimeException("Content read fail");
            }
        }
    }

    //十六进制字符到数字的转换
    private byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9')) return (byte) (b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte) (b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte) (b - 'A' + 10);
        return 0;
    }

    public void parseParameters(Map<String, String[]> map, byte[] data, String encoding)
            throws UnsupportedEncodingException {
        if (parsed) {
            return;
        }

        if (data != null && data.length > 0) {
            int pos = 0;
            int ix = 0;
            int ox = 0;
            String key = null;
            String value = null;

            //解析参数串，处理特殊字符
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                    case '&':   //两个参数之间的分隔符，遇到这个字符保存已经解析的key和value
                        value = new String(data, 0, ox, encoding);
                        if (key != null) {
                            putMapEntry(map, key, value);
                            key = null;
                        }
                        ox = 0;
                        break;
                    case '=': //参数的key/value的分隔符
                        key = new String(data, 0, ox, encoding);
                        ox = 0;
                        break;
                    case '+': //特殊字符，空格
                        data[ox++] = (byte) ' ';
                        break;
                    case '%': // %NN 代表 ASCII 字符
                        // 十六进制 => 十进制 => refer to ASCII 码表
                        data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4)
                                + convertHexDigit(data[ix++]));
                        break;
                    default:
                        data[ox++] = c;
                }
            }
            //最后一个参数没有&结尾
            //The last value does not end in '&'.  So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }

        parsed = true;
    }

    //给key设置新值，多值用数组来存储
    private static void putMapEntry(Map<String, String[]> map, String name, String value) {
        String[] newValues;
        String[] oldValues = map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

    public void parseHeaders() throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();
            sis.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                }
                throw new ServletException("httpProcessor.parseHeaders.colon");
            }

            String name = new String(header.name, 0, header.nameEnd).toLowerCase();
            String value = new String(header.value, 0, header.valueEnd);
            headers.put(name, value);

            if (name.equals(DefaultHeaders.COOKIE_NAME)) {
                this.cookies = parseCookieHeader(value);
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("jsessionid")) {
                        this.sessionId = cookie.getValue();
                    }
                }
            } else if (name.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (value.equals("close")) {
                    this.response.setHeader("Connection", "close");
                }
            }

        }
    }

    public void setResponse(HttpResponseImpl response) {
        this.response = response;
    }

    //解析Cookie头，格式为: key1=value1;key2=value2
    public Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.isEmpty())) {
            return (new Cookie[0]);
        }

        ArrayList<Cookie> cookies = new ArrayList<>();
        while (!header.isEmpty()) {
            int semicolon = header.indexOf(';');

            if (semicolon < 0) {
                semicolon = header.length();
            }
            if (semicolon == 0) {
                break;
            }

            String token = header.substring(0, semicolon);
            if (semicolon < header.length()) {
                header = header.substring(semicolon + 1);
            } else {
                header = "";
            }

            try {
                int equals = token.indexOf('=');
                if (equals > 0) {
                    String name = token.substring(0, equals).trim();
                    String value = token.substring(equals + 1).trim();
                    cookies.add(new Cookie(name, value));
                }
            } catch (Throwable ignored) {
            }
        }

        return cookies.toArray(new Cookie[0]);
    }

    @Override
    public String getMethod() {
        return new String(this.requestLine.method, 0, this.requestLine.methodEnd);
    }

    @Override
    public String getAuthType() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return "";
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getPathInfo() {
        return "";
    }

    @Override
    public String getPathTranslated() {
        return "";
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return "";
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return "";
    }

    //如果有存在的session，直接返回，如果没有，创建一个新的session
    @Override
    public HttpSession getSession(boolean var1) {
        if (!var1 || this.standardSessionFacade != null) {
            return this.standardSessionFacade;
        }

        if (this.sessionId != null) {
            this.session = HttpConnector.sessions.get(sessionId);
            if (session == null) {
                this.session = HttpConnector.createSession();
            }
        } else {
            this.session = HttpConnector.createSession();
            this.sessionId = session.getId();
        }

        this.standardSessionFacade = new StandardSessionFacade(session);
        return this.standardSessionFacade;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public HttpSession getSession() {
        return this.session;
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return List.of();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.sis;
    }

    @Override
    public String getParameter(String name) {
        parseParameters();
        String[] values = parameters.get(name);
        if (values != null) {
            return (values[0]);
        }
        return null;
    }

    // post get uri & headers, then get parameters.
    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return this.parameters;
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public String getRemoteHost() {
        return "";
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return "";
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "";
    }

    @Override
    public String getLocalAddr() {
        return "";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws
            IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public Connector getConnector() {
        return null;
    }

    @Override
    public void setConnector(Connector connector) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public String getInfo() {
        return "";
    }

    @Override
    public ServletRequest getRequest() {
        return null;
    }

    @Override
    public Response getResponse() {
        return null;
    }

    @Override
    public void setResponse(Response response) {

    }

    @Override
    public Socket getSocket() {
        return null;
    }

    @Override
    public void setSocket(Socket socket) {

    }

    @Override
    public InputStream getStream() {
        return null;
    }

    @Override
    public Wrapper getWrapper() {
        return null;
    }

    @Override
    public void setWrapper(Wrapper wrapper) {

    }

    @Override
    public ServletInputStream createInputStream() throws IOException {
        return null;
    }

    @Override
    public void finishRequest() throws IOException {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void setContentLength(int length) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setProtocol(String protocol) {

    }

    @Override
    public void setRemoteAddr(String remote) {

    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public void setServerPort(int port) {

    }
}
