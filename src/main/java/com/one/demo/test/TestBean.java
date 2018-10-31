package com.one.demo.test;

import com.one.annotation.OneComponent;

/**
 * @author One
 * @description
 * @date 2018/10/31
 */
@OneComponent("myTest")
public class TestBean {
    private String name;

    @Override
    public String toString() {
        return "TestBean{" +
                "name='" + name + '\'' +
                '}';
    }
}
