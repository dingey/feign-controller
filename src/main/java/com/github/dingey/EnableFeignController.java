package com.github.dingey;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author d
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({FeignControllerRegistrar.class})
public @interface EnableFeignController {
    String[] value() default {};
}
