package com.ellen.tomcat.mini;

import java.io.IOException;
import javax.servlet.ServletException;

// 调用下一个 valve
public interface ValveContext {
    String getInfo();

    void invokeNext(Request request, Response response)
            throws IOException, ServletException;
}
