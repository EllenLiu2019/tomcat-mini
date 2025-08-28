package com.ellen.tomcat.mini;

import javax.servlet.ServletException;
import java.io.IOException;

// 表示 Container 中的一段用户增加的逻辑
public interface Valve {
    String getInfo();

    Container getContainer();

    void setContainer(Container container);

    // core method
    void invoke(Request request, Response response, ValveContext context)
            throws IOException, ServletException;
}
