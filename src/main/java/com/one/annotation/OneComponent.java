package com.one.annotation;

import java.lang.annotation.*;

/**
 * 组件
 * One on 2018/10/31.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneComponent {
    String value() default "";
}
