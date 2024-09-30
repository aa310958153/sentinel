package com.alibaba.csp.sentinel.dashboard.auth;

import com.alibaba.fastjson.JSON;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LogHandlerInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(LogHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        String uri = request.getRequestURI();
        String url = String.valueOf(request.getRequestURL());
        String method = request.getMethod();

        // 获取请求体
        String requestBody = byteToUtf8String(readRequestBody(request));

        // 获取请求头
        Map<String, String> headerMap = getHeaderMap(request);

        logger.info("当前访问路径：{}, url: {}, 方法：{}, 请求参数：{}, 请求头: {}",
            uri, url, method, requestBody, JSON.toJSONString(headerMap));

        return true;
    }

    private String byteToUtf8String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] readRequestBody(HttpServletRequest request) throws IOException {
        if (request.getClass().isAssignableFrom(RequestWrapper.class)) {
            RequestWrapper requestWrapper = (RequestWrapper) request;
            return requestWrapper.getBody();
        }
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ServletInputStream inputStream = request.getInputStream();
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Map<String, String> getHeaderMap(HttpServletRequest request) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }
        return headerMap;
    }
}
