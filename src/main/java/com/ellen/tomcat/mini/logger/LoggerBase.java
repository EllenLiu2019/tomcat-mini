package com.ellen.tomcat.mini.logger;

import com.ellen.tomcat.mini.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.ServletException;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

public abstract class LoggerBase implements Logger {

    @Getter
    protected final String info = "com.ellen.tomcat.mini.logger.LoggerBase/1.0";

    @Getter
    @Setter
    protected int verbosity = ERROR;

    @Getter
    @Setter
    protected int debug = 0;



    public void setVerbosityLevel(String verbosity) {
        if ("FATAL".equalsIgnoreCase(verbosity))
            this.verbosity = FATAL;
        else if ("ERROR".equalsIgnoreCase(verbosity))
            this.verbosity = ERROR;
        else if ("WARNING".equalsIgnoreCase(verbosity))
            this.verbosity = WARNING;
        else if ("INFORMATION".equalsIgnoreCase(verbosity))
            this.verbosity = INFORMATION;
        else if ("DEBUG".equalsIgnoreCase(verbosity))
            this.verbosity = DEBUG;
    }

    // 此方法由具体实现类实现
    public abstract void log(String msg);

    public void log(Exception exception, String msg) {
        this.log(msg, exception);
    }

    //核心方法，printStackTrace，然后调用一个业务实现的log(msg)
    public void log(String msg, Throwable throwable) {
        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(msg);
        throwable.printStackTrace(writer);

        Throwable rootCause = null;
        if (throwable instanceof ServletException) {
            rootCause = ((ServletException) throwable).getRootCause();
        }
        if (rootCause != null) {
            writer.println("----- Root Cause -----");
            rootCause.printStackTrace(writer);
        }

        log(buf.toString());
    }

    public void log(String message, int verbosity) {
        if (this.verbosity >= verbosity) {
            log(message);
        }
    }

    public void log(String message, Throwable throwable, int verbosity) {
        if (this.verbosity >= verbosity) {
            log(message, throwable);
        }
    }
}
