package server;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;


@Getter
public class Response implements ServletResponse {

    @Setter
    Request request;

    OutputStream outputStream;

    PrintWriter writer;

    String contentType;

    long contentLength = -1;

    String charset;

    String characterEncoding;

    //以输出流作为接收参数
    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }


    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        //autoFlush: true, println、printf 等方法会自动刷新输出流的缓冲。
        this.writer = new PrintWriter(new OutputStreamWriter(this.outputStream, this.getCharacterEncoding()), true);
        return this.writer;
    }

    @Override
    public void setCharacterEncoding(String var1) {
        this.characterEncoding = var1;
    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentLengthLong(long l) {

    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
