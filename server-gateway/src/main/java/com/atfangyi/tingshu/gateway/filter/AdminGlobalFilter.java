package com.atfangyi.tingshu.gateway.filter;

import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.common.util.IpUtil;
import com.atfangyi.tingshu.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @className: AdminGlobalFilter
 * @author: 方毅
 * @date: 2025/12/10 17:47
 * @version: 1.0
 * @description: TODO
 */

@Component
@Slf4j
public class AdminGlobalFilter implements GlobalFilter {

    @Value("${ai.service.token:}")
    private String aiServiceToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 放行一些接口 login / register
        String path = exchange.getRequest().getURI().getPath();
        if (!path.contains("admin")) return chain.filter(exchange);
        if (path.contains("code")) return chain.filter(exchange);
        if (path.contains("login") || path.contains("register")) {
            log.info("访问接口{} ,, ip{}", path, IpUtil.getGatwayIpAddress(exchange.getRequest()));
            return chain.filter(exchange);
        }

        // AI服务内部调用认证（特殊header）
        String aiToken = exchange.getRequest().getHeaders().getFirst("X-AI-Service-Token");
        if (aiToken != null && aiToken.equals(aiServiceToken)) {
            log.info("AI服务内部调用: {}", path);
            return chain.filter(exchange);
        }

        // 鉴权token
        String token;
        List<String> strings = exchange.getRequest().getHeaders().get("token");
        if (strings == null || strings.isEmpty()) {
            log.warn("请求{} 缺少token, ip {}", path, IpUtil.getGatwayIpAddress(exchange.getRequest()));
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        token = strings.get(0);
        Assert.notNull(token, "token不能为空");
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("token解析出现错误{}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
    }
}
