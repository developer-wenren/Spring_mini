package com.one.ioc;

import com.one.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author One
 * @description
 * @date 2018/11/01
 */
public class BeanDefinitionReader {

    private static final String SCAN_PACKAGE = "locations";

    private List<String> classNames = new ArrayList<>();
    private Properties contextConfig = new Properties();
    private List<String> locations = null;

    public BeanDefinitionReader(String[] locations) {
        this.locations = Arrays.asList(locations);
    }

    public void configureLocations() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (String location : locations) {
            InputStream stream = contextClassLoader.getResourceAsStream(location);
            try {
                contextConfig.load(stream);
            } catch (IOException e) {
                System.out.println("配置文件未找到");
                e.printStackTrace();
            }
        }
    }

    private List<String> loadBeanDefinition(String packageName) {
        //根据配置获取所有要加载的类
        String filePath = packageName.replaceAll("\\.", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath + "/");
        File fileDir = new File(url.getPath().replace("%20", " "));
        boolean exists = fileDir.exists();
        if (fileDir != null) {
            File[] files = fileDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        loadBeanDefinition(packageName + "." + file.getName());
                        System.out.println(file.getName());
                    } else {
                        classNames.add(packageName + "." + file.getName().replace(".class", ""));
                    }
                }
            }
        }
        return classNames;
    }

    public BeanDefinition registerBeanDefinition(String className) {
        if (!classNames.contains(className)) {
            return null;
        }
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className.trim());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(className);
        String beanName = StringUtil.lowerFirstLetter(className);
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClass(aClass);
        return beanDefinition;
    }

    public List<String> loadBeanDefinitionClassNames() {
        return loadBeanDefinition(contextConfig.getProperty(SCAN_PACKAGE));
    }
}
