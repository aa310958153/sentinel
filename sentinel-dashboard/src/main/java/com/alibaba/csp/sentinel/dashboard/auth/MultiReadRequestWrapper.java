package com.alibaba.csp.sentinel.dashboard.auth;

/**
 * @author qiang.li  解决因为ServletInputStream 读取body流 读取后指针不能还原其他地方不能读取多次
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class MultiReadRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;
    private Map<String, String[]> parameterMap;

    public byte[] getBody() {
        return body;
    }

    public MultiReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 读取请求体并存储在内存中
        body = toByteArray(request.getInputStream());
        // 解析请求体并缓存参数
        parseParameters();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameterMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    private void parseParameters() {
        if (parameterMap != null) {
            return;
        }

        parameterMap = new HashMap<>();
        String contentType = getContentType();
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
            try {
                String bodyString = new String(body, getCharacterEncoding());
                parseUrlEncodedParameters(bodyString, parameterMap);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse parameters", e);
            }
        } else {
            // 如果不是 application/x-www-form-urlencoded 类型，使用默认的参数解析
            parameterMap.putAll(super.getParameterMap());
        }
    }

    private void parseUrlEncodedParameters(String bodyString, Map<String, String[]> parameterMap) {
        String[] pairs = bodyString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                try {
                    value = URLDecoder.decode(value, getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to decode parameter value", e);
                }
                String[] existingValues = parameterMap.get(key);
                if (existingValues == null) {
                    parameterMap.put(key, new String[]{value});
                } else {
                    String[] newValues = Arrays.copyOf(existingValues, existingValues.length + 1);
                    newValues[newValues.length - 1] = value;
                    parameterMap.put(key, newValues);
                }
            }
        }
    }

    private byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }
}
