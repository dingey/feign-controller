package com.github.dingey;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

public class FeignControllerFactoryBean<T> implements FactoryBean<T>, InitializingBean {
    /**
     * 代理的接口
     */
    private Class<T> reqInterface;
    /**
     * 代理对象
     */
    private T reqObject;

    /**
     * 代理对象
     */
    private T fallbackObject;

    private ApplicationContext context;
    private RestTemplate restTemplate;

    @Override
    public T getObject() throws Exception {
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return reqInterface;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
