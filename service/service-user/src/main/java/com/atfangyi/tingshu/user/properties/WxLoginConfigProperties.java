package com.atfangyi.tingshu.user.properties;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "wx.manager.open")
@Data
public class WxLoginConfigProperties {

    private String appId;

    private String appSecret;

    private String redirectUrl;
}
