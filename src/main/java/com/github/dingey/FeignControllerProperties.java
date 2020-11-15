package com.github.dingey;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "feign.controller")
public class FeignControllerProperties {
    /**
     * 需要注册为controller的feign接口所在包路径
     */
    private String[] paths;


}