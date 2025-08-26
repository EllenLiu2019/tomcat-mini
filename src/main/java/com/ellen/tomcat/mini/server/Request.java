package com.ellen.tomcat.mini.server;

import java.io.InputStream;

public class Request {

    private InputStream input;

    private String url;


    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        // 解析Http请求
        StringBuilder request = new StringBuilder(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        System.out.print(request);
        // 获取URL
        url = parseUrl(request.toString());
    }

    private String parseUrl(String requestString) {
        int index1 = requestString.indexOf(" ");
        if (index1 != -1) {
            int index2 = requestString.indexOf(" ", index1 + 1);
            if (index2 > index1) {
                return requestString.substring(index1 + 1, index2);
            }
        }
        return null;
    }

    public String getUrl() {
        return url;
    }
}
