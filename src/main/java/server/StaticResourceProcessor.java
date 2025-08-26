package server;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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

    public void process(Request request, Response response) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fileInputStream = null;
        OutputStream output;
        try {
            output = response.getOutput();
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if (file.exists()) {
                String head = composeResponseHead(file);
                output.write(head.getBytes(StandardCharsets.UTF_8));

                fileInputStream = new FileInputStream(file);
                int ch = fileInputStream.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fileInputStream.read(bytes, 0, BUFFER_SIZE);
                }

                output.flush();
            } else {
                String errorMessage = """
                        HTTP/1.1 404 FIle Not Found\r
                        Content-Type: text/html\r
                        Content-Length: 23\r
                        \r
                        <h1>File Not Found</h1>""";
                output.write(errorMessage.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private String composeResponseHead(File file) {
        Map<String, Object> values = new HashMap<>();
        values.put("StatusCode", "200");
        values.put("StatusName", "OK");
        values.put("ContentType", "text/html;charset=utf-8");
        values.put("ContentLength", file.length());
        values.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(values);
        return sub.replace(OK_Message);
    }

}
