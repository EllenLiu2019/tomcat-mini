package com.ellen.tomcat.mini.connector.http;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StaticResourceProcessor {

    private final static String OK_Message = """
            HTTP/1.1 ${StatusCode} ${StatusName}\r
            Content-Type: ${ContentType}\r
            Content-Length: ${ContentLength}\r
            Server: mini\r
            Date: ${ZonedDateTime}\r
            \r
            """;

    private final static String errorMessage = """
            HTTP/1.1 404 FIle Not Found\r
            Content-Type: text/html\r
            Content-Length: 23\r
            \r
            <h1>File Not Found</h1>""";

    public void process(HttpRequestImpl request, HttpResponseImpl response) throws IOException {
        byte[] bytes;
        PrintWriter writer;
        String path = request.getUri().substring(1);
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(path)) {
            response.setCharacterEncoding("UTF-8");
            writer = response.getWriter();
            if (is != null) {
                bytes = is.readAllBytes();
                writer.println(new String(bytes, StandardCharsets.UTF_8));

            } else {
                writer.println(errorMessage);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String composeResponseHead(long length) {
        Map<String, Object> values = new HashMap<>();
        values.put("StatusCode", "200");
        values.put("StatusName", "OK");
        values.put("ContentType", "text/html;charset=utf-8");
        values.put("ContentLength", length);
        values.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(values);
        return sub.replace(OK_Message);
    }

}
