package com.ellen.tomcat.mini.logger;

public class SystemOutLogger extends LoggerBase {

    protected static final String info =
            "com.ellen.tomcat.mini.logger.SystemOutLogger/1.0";

    @Override
    public void log(String msg) {
        System.out.println(msg);
    }
}