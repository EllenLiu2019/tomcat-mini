package com.ellen.tomcat.mini.connector.http;

import com.ellen.tomcat.mini.Loader;
import com.ellen.tomcat.mini.loader.WebappLoader;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.ellen.tomcat.mini.startup.Bootstrap.WEB_ROOT;

public class StaticResourceProcessor {

    private static final int BUFFER_SIZE = 1024;

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
        byte[] bytes = new byte[BUFFER_SIZE];
        OutputStream output = response.getOutput();
        String path = request.getDocbase() + request.getUri();
        File file = new File(WEB_ROOT, path);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                int ch;
                while ((ch = fis.read(bytes, 0, BUFFER_SIZE)) != -1) {
                    output.write(bytes, 0, ch);
                }
                output.flush();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            output.write(errorMessage.getBytes(StandardCharsets.UTF_8));
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
