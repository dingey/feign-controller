package com.github.dingey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableFeignController
@EnableConfigurationProperties({FeignControllerProperties.class})
public class FeignControllerAutoConfiguration {
}
