package com.one.ioc;

/**
 * @author One
 * @description 工厂接口
 * @date 2018/11/01
 */
public interface BeanFactory {

    /**
     * 根据名称获取 bean
     *
     * @param name
     * @return
     */
    Object getBean(String name);

    /**
     * 根据类型获取 bean
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getBean(Class<T> clazz);

}
