package com.one.demo;

import com.one.annotation.OneAutowired;
import com.one.annotation.OneComponent;
import com.one.demo.test.TestBean;

/**
 * @author One
 * @description
 * @date 2018/10/31
 */
@OneComponent
public class DemoBean {

    @OneAutowired
    private TestBean testBean;

    private String name;

    public TestBean getTestBean() {
        return testBean;
    }

    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DemoBean{" +
                "testBean=" + testBean +
                ", name='" + name + '\'' +
                '}';
    }
}
