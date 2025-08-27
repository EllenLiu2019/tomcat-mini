package com.ellen.tomcat.mini.connector.http;

import com.ellen.tomcat.mini.util.CookieTools;
import lombok.Setter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HttpResponseImpl implements HttpServletResponse {

    @Setter
    HttpRequestImpl request;

    OutputStream outputStream;

    PrintWriter writer;

    String contentType;

    long contentLength = -1;

    String charset;

    String characterEncoding = "UTF-8";

    Map<String, String> headers = new ConcurrentHashMap<>();

    static String protocol = "HTTP/1.1";

    String message = this.getStatusMessage(HttpServletResponse.SC_OK);

    int status = HttpServletResponse.SC_OK;

    final Set<Cookie> cookies = new HashSet<>();


    public HttpResponseImpl() {}

    public HttpResponseImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    protected String getStatusMessage(int status) {
        return switch (status) {
            case SC_OK -> ("OK");
            case SC_ACCEPTED -> ("Accepted");
            case SC_BAD_GATEWAY -> ("Bad Gateway");
            case SC_BAD_REQUEST -> ("Bad Request");
            case SC_CONTINUE -> ("Continue");
            case SC_FORBIDDEN -> ("Forbidden");
            case SC_INTERNAL_SERVER_ERROR -> ("Internal Server Error");
            case SC_METHOD_NOT_ALLOWED -> ("Method Not Allowed");
            case SC_NOT_FOUND -> ("Not Found");
            case SC_NOT_IMPLEMENTED -> ("Not Implemented");
            case SC_REQUEST_URI_TOO_LONG -> ("Request URI Too Long");
            case SC_SERVICE_UNAVAILABLE -> ("Service Unavailable");
            case SC_UNAUTHORIZED -> ("Unauthorized");
            default -> ("HTTP Response Status " + status);
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        //autoFlush: true, println、printf 等方法会自动刷新输出流的缓冲。
        this.writer = new PrintWriter(new OutputStreamWriter(this.outputStream, this.getCharacterEncoding()), true);
        return this.writer;
    }

    public void sendHeaders() throws IOException {
        PrintWriter outputWriter = this.getWriter();

        outputWriter.print(this.getProtocol());
        outputWriter.print(" ");
        outputWriter.print(status);
        if (message != null) {
            outputWriter.print(" ");
            outputWriter.print(message);
        }
        outputWriter.print("\r\n");

        if (getContentType() != null) {
            outputWriter.print("Content-Type: " + getContentType() + "\r\n");
        }
        if (getContentLength() >= 0) {
            outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
        }

        for (String name : headers.keySet()) {
            String value = headers.get(name);
            outputWriter.print(name);
            outputWriter.print(": ");
            outputWriter.print(value);
            outputWriter.print("\r\n");
        }

        HttpSession session = this.request.getSession(false);
        if (session != null) {
            Cookie cookie = new Cookie(DefaultHeaders.JSESSIONID_NAME, session.getId());
            cookie.setMaxAge(-1);
            addCookie(cookie);
        }
        synchronized (this.cookies) {
            for (Cookie cookie : cookies) {
                outputWriter.print(CookieTools.getCookieHeaderName(cookie));
                outputWriter.print(": ");

                StringBuffer sbValue = new StringBuffer();
                CookieTools.getCookieHeaderValue(cookie, sbValue);
                System.out.println("set cookie jsessionid string : " + sbValue);

                outputWriter.print(sbValue);
                outputWriter.print("\r\n");
            }
        }

        //最后输出空行
        outputWriter.print("\r\n");
        outputWriter.flush();
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_LENGTH_NAME)) {
            setContentLength(Integer.parseInt(value));
        }
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_TYPE_NAME)) {
            setContentType(value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_LENGTH_NAME)) {
            setContentLength(Integer.parseInt(value));
        }
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_TYPE_NAME)) {
            setContentType(value);
        }
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String var1) {
        this.characterEncoding = var1;
    }

    @Override
    public void setContentLength(int var1) {
        this.contentLength = var1;
    }

    @Override
    public void setContentType(String var1) {
        this.contentType = var1;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getHeader(String var1) {
        return headers.get(var1);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public void addCookie(Cookie cookie) {
        synchronized (this.cookies) {
            cookies.add(cookie);
        }
    }

    public void finishResponse() {
        try {
            this.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return "";
    }

    @Override
    public String encodeRedirectURL(String s) {
        return "";
    }

    @Override
    public String encodeUrl(String s) {
        return "";
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return "";
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void setIntHeader(String s, int i) {

    }

    @Override
    public void addIntHeader(String s, int i) {

    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public void setStatus(int i, String s) {

    }

    @Override
    public Collection<String> getHeaders(String var1) {
        return List.of();
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public void setContentLengthLong(long l) {

    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
