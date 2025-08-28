package com.ellen.tomcat.mini.logger;

import com.ellen.tomcat.mini.util.StringManager;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class FileLogger extends LoggerBase {

    protected static final String info = "com.ellen.tomcat.mini.logger.FileLogger/0.1";

    private String date = "";

    @Getter
    @Setter
    private String directory = "logs";

    @Getter
    @Setter
    private String prefix = "com.ellen.tomcat.mini.";

    private StringManager sm = StringManager.getManager(Constants.Package);
    private boolean started = false;

    @Getter
    @Setter
    private String suffix = ".log";

    @Getter
    @Setter
    private boolean timestamp = true;
    private PrintWriter writer;

    public void log(String msg) {
        // 当前时间Construct the timestamp we will use, if requested
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        String tsDate = tsString.substring(0, 10);

        // 如果日期变化了，新生成一个log文件
        // If the date has changed, switch log files
        if (!date.equals(tsDate)) {
            synchronized (this) {
                if (!date.equals(tsDate)) {
                    close();
                    date = tsDate;
                    open();
                }
            }
        }

        // 记录日志，带上时间戳
        if (writer != null) {
            if (timestamp) {
                writer.println(tsString + " " + msg);
            } else {
                writer.println(msg);
            }
        }
    }

    private void close() {
        if (writer == null)
            return;
        writer.flush();
        writer.close();
        writer = null;
        date = "";
    }

    private void open() {
        File dir = new File(directory);
        if (!dir.isAbsolute())
            dir = new File(System.getProperty("catalina.base"), directory);
        dir.mkdirs();

        // 打开日志文件
        // Open the current log file
        try {
            String pathname = dir.getAbsolutePath() +
                    File.separator + prefix + date + suffix;
            writer = new PrintWriter(new FileWriter(pathname, true), true);
        } catch (IOException e) {
            writer = null;
        }
    }
}