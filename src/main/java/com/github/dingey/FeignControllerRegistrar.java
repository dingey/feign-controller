package com.github.dingey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;

public class FeignControllerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware, ApplicationContextAware {
    private ResourceLoader resourceLoader;
    private ApplicationContext context;
    private FeignControllerProperties properties;

    Log log = LogFactory.getLog(this.getClass());


    @Override
    public void setEnvironment(@NonNull Environment environment) {
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setProperties(FeignControllerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableFeignController.class.getName()));
        ClassPathScanner scanner = new ClassPathScanner(registry);
        // this check is needed in Spring 3.1
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        List<String> basePackages = new ArrayList<>();
        if ((annoAttrs != null ? annoAttrs.getStringArray("value").length : 0) > 0) {
            for (String pkg : annoAttrs.getStringArray("value")) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }
        } else {
            if (properties != null && properties.getPaths() != null && properties.getPaths().length > 0) {
                basePackages.addAll(Arrays.asList(properties.getPaths()));
            } else {
                basePackages.add(ClassUtils.getPackageName(annotationMetadata.getClassName()));
            }
        }
        scanner.doScan(StringUtils.toStringArray(basePackages));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }


    class ClassPathScanner extends ClassPathBeanDefinitionScanner {

        ClassPathScanner(BeanDefinitionRegistry registry) {
            super(registry,false);
            addIncludeFilter((metadataReader, metadataReaderFactory) -> metadataReader.getAnnotationMetadata().isAnnotated(FeignClient.class.getName()));
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(@NonNull String... basePackages) {
            Set<BeanDefinition> candidateComponents = findCandidateComponents(basePackages[0]);

            return Collections.emptySet();
        }


        /**
         * 默认情况下只有顶层具体类才会通过
         * 只返回是接口的beanDefinition
         *
         * @param beanDefinition bean
         * @return true / false
         */
        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface()
                    && beanDefinition.getMetadata().isIndependent();
        }
    }

    /**
     * 去掉Controller的Mapping
     */
    public void unregisterController(String controllerBeanName) {
        final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                context.getBean("requestMappingHandlerMapping");
        Object controller = context.getBean(controllerBeanName);
        final Class<?> targetClass = controller.getClass();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            try {
                Method createMappingMethod = RequestMappingHandlerMapping.class.
                        getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                createMappingMethod.setAccessible(true);
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                        createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                if (requestMappingInfo != null) {
                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                }
            } catch (Exception e) {
                log.error(String.format("无法取消注册 %s ，原因: %s", controllerBeanName, e.getMessage()));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
    }

    /**
     * 注册Controller
     */
    public void registerController(String controllerBeanName) {
        final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");

        unregisterController(controllerBeanName);
        try {
            //注册Controller
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().
                    getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, controllerBeanName);
        } catch (Exception e) {
            log.error(String.format("无法注册 %s 为controller，原因: %s", controllerBeanName, e.getMessage()));
        }
    }
}
