package com.xwq;


import com.xwq.annotation.Aspect;
import com.xwq.annotation.Component;
import com.xwq.annotation.Repository;
import com.xwq.annotation.Service;
import com.xwq.spring.BeanFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ApplicationContext {
    static {
        try {
            String packageName = ApplicationContext.class.getPackage().getName();
            String packagePath = packageName.replace(".", "/");
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = loader.getResources(packagePath);

            List<String> classNameList = new ArrayList<String>();

            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if(url == null) continue;

                String path = url.getPath();

                classNameList.addAll(getClassNameFromFile(path));
            }

            for(String className : classNameList) {
                Class<?> clz = Class.forName(className);

                if(clz.isAnnotationPresent(Component.class)) {
                    Component annotation = clz.getAnnotation(Component.class);
                    String value = annotation.value();

                    createBean(className, value);
                }
                if(clz.isAnnotationPresent(Service.class)) {
                    Service annotation = clz.getAnnotation(Service.class);
                    String value = annotation.value();

                    createBean(className, value);
                }
                if(clz.isAnnotationPresent(Repository.class)) {
                    Repository annotation = clz.getAnnotation(Repository.class);
                    String value = annotation.value();

                    createBean(className, value);
                }
            }

            //aop
            for(String className : classNameList) {
                Class<?> clz = Class.forName(className);

                if(clz.isAnnotationPresent(Aspect.class)) {
                    Aspect aspect = clz.getAnnotation(Aspect.class);
                    String pointcutClassName = aspect.value();

                    if(StringUtils.isNotBlank(pointcutClassName)) {
                        Object target = BeanFactory.getBeanByClassName(pointcutClassName);

                        Object aspectObj = clz.newInstance();
                        Method method = clz.getMethod("setTarget", Object.class);
                        method.invoke(aspectObj, target);

                        Object proxy = Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), (InvocationHandler) aspectObj);
                        BeanFactory.setProxyByClassName(pointcutClassName, proxy);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createBean(String className, String value) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(StringUtils.isNotBlank(value)) {
            BeanFactory.createBean(value, className);
        } else {
            String beanId = className.substring(className.lastIndexOf(".") + 1);
            BeanFactory.createBean(beanId, className);
        }
    }


    private static List<String> getClassNameFromFile(String filePath) {
        List<String> classNameList = new ArrayList<String>();

        File file = new File(filePath);
        File[] subFiles = file.listFiles();

        if(null == subFiles) return classNameList;

        for(File subFile : subFiles) {
            if(subFile.isDirectory()) {
                classNameList.addAll(getClassNameFromFile(subFile.getPath()));
            } else {
                String subFilePath = subFile.getPath();

                if(subFilePath.endsWith(".class")) {
                    String classPath = subFilePath.substring(subFilePath.indexOf("\\classes\\") + 9, subFilePath.lastIndexOf("."));
                    String className = classPath.replace("\\", ".");
                    classNameList.add(className);
                }
            }
        }
        return classNameList;
    }


    public static <T> T getBean(String beanId, Class<T> clz) {
        Object bean = BeanFactory.getBean(beanId);
        return (T) bean;
    }

    public static <T> T getBean(Class<T> clz) {
        return BeanFactory.getBean(clz);
    }
}
