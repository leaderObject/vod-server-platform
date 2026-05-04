package com.atfangyi.agent.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI服务内部HTTP客户端，自动携带认证token
 */
@Slf4j
@Component
public class AiHttpClient {

    @Value("${tool.gateway-url:http://localhost:8500}")
    private String gatewayUrl;

    @Value("${tool.ai-service-token:}")
    private String aiServiceToken;

    private static final String AI_SERVICE_TOKEN_HEADER = "X-AI-Service-Token";

    public String get(String path) {
        try {
            String url = gatewayUrl + path;
            HttpResponse response = HttpRequest.get(url)
                    .header(AI_SERVICE_TOKEN_HEADER, aiServiceToken)
                    .timeout(10000)
                    .execute();
            return response.body();
        } catch (Exception e) {
            log.error("HTTP GET请求失败: {}, error: {}", path, e.getMessage());
            return null;
        }
    }

    public String post(String path, String body) {
        try {
            String url = gatewayUrl + path;
            HttpResponse response = HttpRequest.post(url)
                    .header(AI_SERVICE_TOKEN_HEADER, aiServiceToken)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .timeout(10000)
                    .execute();
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST请求失败: {}, error: {}", path, e.getMessage());
            return null;
        }
    }

    public String postNoBody(String path) {
        try {
            String url = gatewayUrl + path;
            HttpResponse response = HttpRequest.post(url)
                    .header(AI_SERVICE_TOKEN_HEADER, aiServiceToken)
                    .header("Content-Type", "application/json")
                    .body("{}")
                    .timeout(10000)
                    .execute();
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST请求失败: {}, error: {}", path, e.getMessage());
            return null;
        }
    }
}
