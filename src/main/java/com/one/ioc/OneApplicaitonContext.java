package com.one.ioc;

import com.one.StringUtil;
import com.one.annotation.OneAutowired;
import com.one.annotation.OneComponent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author One
 * @description 自定义ioc 的上下文
 * @date 2018/10/31
 */
public class OneApplicaitonContext implements BeanFactory {
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<String> beanDefinitionClassNames = new ArrayList<>();
    private BeanDefinitionReader beanDefinitionReader;
    private Map<String, Object> instanceCache = new ConcurrentHashMap<>();
    private Map<Class<?>, BeanDefinition> classCache = new ConcurrentHashMap<>();
    private Map<String, BeanWrapperImpl> beanWrapperMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        OneApplicaitonContext applicaitonContext = new OneApplicaitonContext("application.properties");
        Object demoBean = applicaitonContext.getBean("demoBean");
        System.out.println(demoBean);
//        TestBean bean = applicaitonContext.getBean(TestBean.class);
//        System.out.println(bean);
    }

    @Override
    public Object getBean(String beanName) {
        return doGetBean(beanName, null);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return (T) doGetBean(null, requiredType);
    }

    private Object doGetBean(String beanName, Class<?> clazz) {
        if (StringUtil.isNotBank(beanName) && clazz == null) {
            if (instanceCache.containsKey(beanName)) {
                return instanceCache.get(beanName);
            }
            // 初始化对象
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            return createBean(beanDefinition);
        }

        if (!StringUtil.isNotBank(beanName) && clazz != null) {
            BeanDefinition beanDefinition = classCache.get(beanName);
            if (beanDefinition != null) {
                beanName = beanDefinition.getFactoryBeanName();
                if (instanceCache.containsKey(beanName)) {
                    return instanceCache.get(beanName);
                }
                return createBean(beanDefinition);
            }
        }
        return null;
    }

    private Object createBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getFactoryBeanName();
        Object instacne = instantiate(beanDefinition);
        if (instacne == null) {
            return null;
        }

        // 对实例进行包装,用于增强
        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
        beanPostProcessor.postProcessBeforeInitialization(instacne,beanName);
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(instacne);
        beanWrapperMap.put(beanName, beanWrapper);

        //依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);
        beanPostProcessor.postProcessAfterInitialization(instacne,beanName);
        return beanWrapper.getRootObject();
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapperImpl beanWrapper) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            boolean annotationPresent = field.isAnnotationPresent(OneAutowired.class);
            if (annotationPresent) {
                Object obj = beanWrapperMap.get(field.getName());
                if (obj == null) {
                    obj = getBean(field.getName());
                }
                try {
                    field.set(beanWrapper.getRootObject(), obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public OneApplicaitonContext(String... locations) {
        System.out.println("开始启动======");
        configureLocations(locations);
        refresh();
        System.out.println("启动结束======");
        System.out.println(beanDefinitionClassNames);
        System.out.println(beanDefinitionMap);
        System.out.println(beanWrapperMap);
        System.out.println(instanceCache);
        System.out.println(classCache);
    }

    private void configureLocations(String[] locations) {
        beanDefinitionReader = new BeanDefinitionReader(locations);
    }


    private void refresh() {
        // 加载定位注册
        readBeanDefinition();
        loadBeanDefinitionClassNames();
        registerBeanDefinition();
        instantiateBeans();
    }

    private Object instantiate(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanClass();
        String beanName = beanDefinition.getFactoryBeanName();

        boolean annotationPresent = clazz.isAnnotationPresent(OneComponent.class);
        if (annotationPresent) {
            try {
                Object instance = clazz.newInstance();
                instanceCache.put(beanName, instance);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void instantiateBeans() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            //懒加载跳过初始化
            if (beanDefinition.isLazyInit()) {
                continue;
            }
            getBean(beanName);
        }
    }

    private void readBeanDefinition() {
        beanDefinitionReader.configureLocations();
    }

    private void loadBeanDefinitionClassNames() {
        beanDefinitionClassNames = beanDefinitionReader.loadBeanDefinitionClassNames();
    }

    private void registerBeanDefinition() {
        for (String className : beanDefinitionClassNames) {
            BeanDefinition beanDefinition = beanDefinitionReader.registerBeanDefinition(className);
            if (beanDefinition != null) {
                beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                classCache.put(beanDefinition.getBeanClass(), beanDefinition);
            }
        }
    }


    public void init() throws ClassNotFoundException {
        // 定位
//        doLocateResource();
//        System.out.println(contextConfig);
        // 加载
//        String locations = contextConfig.getProperty("locations");
//        doLoadResource(locations);
//        System.out.println(classNames);

        // 注册
//        doRegister();
    }

//    private void doRegister() {
//        for (String name : classNames) {
//            try {
//                //创建对象
//                Class<?> aClass = Class.forName(name);
//                Object instance = aClass.newInstance();
//                OneComponent oneComponent = aClass.getAnnotation(OneComponent.class);
//                if (oneComponent != null) {
//                    String beanName;
//                    if (oneComponent.value() == null || oneComponent.value().equals("")) {
//                        beanName = lowerFirstLetter(name);
//                    } else {
//                        beanName = oneComponent.value();
//                    }
//                    ioc.put(beanName, instance);
//                }
//                System.out.println("注入前 :" + ioc);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            }
//        }
//
//        for (Object object : ioc.values()) {
//            Field[] fields = object.getClass().getDeclaredFields();
//            for (Field field : fields) {
//                field.setAccessible(true);
//                OneAutowired oneAutowired = field.getAnnotation(OneAutowired.class);
//                String fieldName = field.getName();
//                String beanName = null;
//                if (oneAutowired != null && oneAutowired.required() == true) {
//                    System.out.println(fieldName);
//                    OneComponent oneComponent = field.getType().getAnnotation(OneComponent.class);
//                    if (oneComponent.value() == null || oneComponent.value().equals("")) {
//                        beanName = lowerFirstLetter(fieldName);
//                    } else {
//                        String value = oneComponent.value();
//                        beanName = value;
//                    }
//                    Object fieldObj = ioc.get(beanName);
//                    PropertyDescriptor propertyDescriptor = null;
//                    try {
//                        propertyDescriptor = new PropertyDescriptor(fieldName, object.getClass());
//                    } catch (IntrospectionException e) {
//                        e.printStackTrace();
//                    }
//                    Method writeMethod = propertyDescriptor.getWriteMethod();
//                    try {
//                        writeMethod.invoke(object, fieldObj);
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    } catch (InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        System.out.println("注入后 :" + ioc);
//    }


//    private void doLoadResource(String packageName) {
//        //根据配置获取所有要加载的类
//        String filePath = packageName.replaceAll("\\.", "/");
//        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath + "/");
//        File fileDir = new File(url.getPath().replace("%20", " "));
//        boolean exists = fileDir.exists();
//        if (fileDir != null) {
//            File[] files = fileDir.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isDirectory()) {
//                        doLoadResource(packageName + "." + file.getName());
//                        System.out.println(file.getName());
//                    } else {
//                        classNames.add(packageName + "." + file.getName().replace(".class", ""));
//                    }
//                }
//            }
//        }
//    }

//    private void doLocateResource() {
//        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//        InputStream stream = contextClassLoader.getResourceAsStream("application.properties");
//        try {
//            contextConfig.load(stream);
//        } catch (IOException e) {
//            System.out.println("配置文件未找到");
//            e.printStackTrace();
//        }
//    }

}
