package com.alibaba.csp.sentinel.dashboard.auth;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiang.li  解决因为ServletInputStream 读取body流 读取后指针不能还原其他地方不能读取多次
 */

public class RequestWrapper extends HttpServletRequestWrapper {

    private final Logger logger = LoggerFactory.getLogger(RequestWrapper.class);
    private byte[] body;

    private Reader reader;

    public RequestWrapper(HttpServletRequest request)
        throws IOException {
        super(request);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            request.getReader().lines().forEach(stringBuffer::append);
            body = stringBuffer.toString().getBytes();
        } catch (IllegalStateException e) {
            body = new byte[]{};
            logger.error("{}接口body为空或丢失", request.getRequestURI());
            //部分接口,文件上传,body为空,会报IllegalStateException
        }
    }

    public byte[] getBody() {
        return body;
    }

    public RequestWrapper(HttpServletRequest request, Reader reader) {
        super(request);
        this.reader = reader;
        StringBuffer stringBuffer = new StringBuffer();
        new BufferedReader(reader).lines().forEach(stringBuffer::append);
        body = stringBuffer.toString().getBytes();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (Objects.isNull(reader)) {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        } else {
            return new BufferedReader(reader);
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }
}