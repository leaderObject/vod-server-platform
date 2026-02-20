package com.atfangyi.tingshu.common.config.feign;

/*
 * @Author:  方毅
 * @date:  2025/11/5 22:43
 */

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            String token = servletRequestAttributes.getRequest().getHeader("token");
            if (!StringUtil.isBlank(token)) {
                requestTemplate.header("token", token);
            }
        }
    }
}
