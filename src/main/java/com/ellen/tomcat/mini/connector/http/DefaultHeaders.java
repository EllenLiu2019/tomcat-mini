package com.ellen.tomcat.mini.connector.http;

public class DefaultHeaders {

    static final String HOST_NAME = "host";
    static final String CONNECTION_NAME = "connection";
    static final String ACCEPT_LANGUAGE_NAME = "accept-language";
    static final String CONTENT_LENGTH_NAME = "content-length";
    static final String CONTENT_TYPE_NAME = "content-type";
    /**
     * 分块的长度 —— chunk size ｜ CRLF
     * 分块的数据 —— chunk data ｜ CRLF
     * 分块的长度 —— chunk size ｜ CRLF
     * 分块的数据 —— chunk data ｜ CRLF
     *        ……  // 此处省略
     * 分块的长度 —— chunk size ｜ CRLF
     * 分块的数据 —— chunk data ｜ CRLF
     * 分块的长度 —— 0 ｜ CRLF
     * 结束标识符 —— CRLF
     * ---------------Response Example----------------
     * HTTP/1.1 200 OK
     * Content-Type: text/plain
     * Transfer-Encoding: chunked
     *
     * 35
     * This is the data in the first chunk
     * 26
     * and this is the second one
     * 3
     * con
     * 8
     * sequence
     * 0
     * -------------------------------------------------
     */
    static final String TRANSFER_ENCODING_NAME = "transfer-encoding"; // chunked: 分块发送数据
    static final String COOKIE_NAME = "cookie";
    static final String JSESSIONID_NAME = "jsessionid";

}