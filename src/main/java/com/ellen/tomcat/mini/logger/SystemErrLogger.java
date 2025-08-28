package com.ellen.tomcat.mini.logger;

public class SystemErrLogger extends LoggerBase {

    protected static final String info =
            "com.ellen.tomcat.mini.logger.SystemErrLogger/0.1";

    @Override
    public void log(String msg) {
        System.err.println(msg);
    }
}
