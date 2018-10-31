package com.one.ioc;

import com.one.annotation.OneAutowired;
import com.one.annotation.OneComponent;
import com.one.demo.test.TestBean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author One
 * @description 自定义ioc 的上下文
 * @date 2018/10/31
 */
public class OneApplicaitonContext {
    private Map<String, Object> ioc = new ConcurrentHashMap<>();
    private Set<String> classNames = new ConcurrentSkipListSet<>();
    private Properties contextConfig = new Properties();

    public static void main(String[] args) {
        OneApplicaitonContext applicaitonContext = new OneApplicaitonContext();
        Object demoBean = applicaitonContext.getBean("demoBean");
        System.out.println(demoBean);
        TestBean bean = applicaitonContext.getBean(TestBean.class);
        System.out.println(bean);
    }

    private Object getBean(String demoBean) {
        Object o = ioc.get(demoBean);
        if (o == null) {
            throw new RuntimeException("未找到该 bean");
        }
        return o;
    }

    public <T> T getBean(Class<T> requiredType) {
        OneComponent annotation = requiredType.getAnnotation(OneComponent.class);
        if (annotation != null) {
            if (annotation.value() != null && !annotation.value().equals("")) {
                return (T) ioc.get(annotation.value());
            } else {
                return (T) ioc.get(lowerFirstLetter(requiredType.getSimpleName()));
            }
        }
        throw new RuntimeException("未找到该 bean");
    }

    public OneApplicaitonContext() {
        System.out.println("开始启动======");
        try {
            init();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("启动结束======");
    }

    public void init() throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.one.demo.DemoBean");
        // 定位
        doLocateResource();
        System.out.println(contextConfig);
        // 加载
        String locations = contextConfig.getProperty("locations");
        doLoadResource(locations);
        System.out.println(classNames);

        // 注册
        doRegister();
    }

    private void doRegister() throws ClassNotFoundException {
        for (String name : classNames) {
            try {
                //创建对象
                Class<?> aClass = Class.forName(name);
                Object instance = aClass.newInstance();
                OneComponent oneComponent = aClass.getAnnotation(OneComponent.class);
                if (oneComponent != null) {
                    String beanName;
                    if (oneComponent.value() == null || oneComponent.value().equals("")) {
                        beanName = lowerFirstLetter(name);
                    } else {
                        beanName = oneComponent.value();
                    }
                    ioc.put(beanName, instance);
                }
                System.out.println("注入前 :" + ioc);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        for (Object object : ioc.values()) {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                OneAutowired oneAutowired = field.getAnnotation(OneAutowired.class);
                String fieldName = field.getName();
                String beanName = null;
                if (oneAutowired != null && oneAutowired.required() == true) {
                    System.out.println(fieldName);
                    OneComponent oneComponent = field.getType().getAnnotation(OneComponent.class);
                    if (oneComponent.value() == null || oneComponent.value().equals("")) {
                        beanName = lowerFirstLetter(fieldName);
                    } else {
                        String value = oneComponent.value();
                        beanName = value;
                    }
                    Object fieldObj = ioc.get(beanName);
                    PropertyDescriptor propertyDescriptor = null;
                    try {
                        propertyDescriptor = new PropertyDescriptor(fieldName, object.getClass());
                    } catch (IntrospectionException e) {
                        e.printStackTrace();
                    }
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    try {
                        writeMethod.invoke(object, fieldObj);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("注入后 :" + ioc);
    }

    private String lowerFirstLetter(String name) {
        String[] split = name.split("\\.");
        String s = split[split.length - 1];
        String first = s.substring(0, 1);
        String substring = s.substring(1, s.length());
        return first.toLowerCase() + substring;
    }

    private void doLoadResource(String packageName) {
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
                        doLoadResource(packageName + "." + file.getName());
                        System.out.println(file.getName());
                    } else {
                        classNames.add(packageName + "." + file.getName().replace(".class", ""));
                    }
                }
            }
        }
    }

    private void doLocateResource() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = contextClassLoader.getResourceAsStream("application.properties");
        try {
            contextConfig.load(stream);
        } catch (IOException e) {
            System.out.println("配置文件未找到");
            e.printStackTrace();
        }
    }

    public static void getAllFileName(String path, ArrayList<String> fileNameList) {
        //ArrayList<String> files = new ArrayList<String>();
        boolean flag = false;
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
//              System.out.println("文     件：" + tempList[i]);
                //fileNameList.add(tempList[i].toString());
                fileNameList.add(tempList[i].getName());
            }
            if (tempList[i].isDirectory()) {
//              System.out.println("文件夹：" + tempList[i]);
                getAllFileName(tempList[i].getAbsolutePath(), fileNameList);
            }
        }
        return;
    }

}
