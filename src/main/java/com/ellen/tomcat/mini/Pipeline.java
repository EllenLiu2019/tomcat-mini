package com.ellen.tomcat.mini;

import java.io.IOException;
import javax.servlet.ServletException;

// Pipeline 代表 Container 中的 Valve 链条
// basic 是特殊的 valve
public interface Pipeline {
    Valve getBasic();

    void setBasic(Valve valve);

    void addValve(Valve valve);

    Valve[] getValves();

    void invoke(Request request, Response response) throws IOException, ServletException;

    void removeValve(Valve valve);
}
