package com.one.annotation;

import java.lang.annotation.*;

/**
 * ${DESCRIPTION}
 * One on 2018/10/31.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneAutowired {
    boolean required() default true;
}
