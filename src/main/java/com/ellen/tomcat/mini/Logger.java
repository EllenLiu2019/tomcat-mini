package com.ellen.tomcat.mini;

public interface Logger {
    int FATAL = Integer.MIN_VALUE;
    int ERROR = 1;
    int WARNING = 2;
    int INFORMATION = 3;
    int DEBUG = 4;

    String getInfo();

    int getVerbosity();

    void setVerbosity(int verbosity);

    void log(String message);

    void log(Exception exception, String msg);

    void log(String message, Throwable throwable);

    void log(String message, int verbosity);

    void log(String message, Throwable throwable, int verbosity);
}