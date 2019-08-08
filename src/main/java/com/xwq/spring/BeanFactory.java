package com.xwq.spring;

import com.xwq.annotation.Autowired;
import com.xwq.exception.MyException;
import com.xwq.util.MapUtil;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    private static Map<String, String> definitionIdClassMap = new ConcurrentHashMap<String, String>();
    private static Map<Class, List<String>> definitionClassIdListMap = new ConcurrentHashMap<Class, List<String>>();
    private static Map<String, List<String>> definitionClassNameIdListMap = new ConcurrentHashMap<String, List<String>>();

    private static Map<String, Object> beanIdMap = new ConcurrentHashMap<String, Object>();
    private static Map<Class, Object> beanClzMap = new ConcurrentHashMap<Class, Object>();

    /**
     * 创建bean
     * @param beanId
     * @param className
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static void createBean(String beanId, String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!beanIdMap.containsKey(beanId)) {
            Class<?> clz = Class.forName(className);
            Object instance = clz.newInstance();

            definitionIdClassMap.put(beanId, className);
            MapUtil.addToMap(definitionClassIdListMap, clz, beanId);
            MapUtil.addToMap(definitionClassNameIdListMap, className, beanId);


            injectFields(clz, instance);

            beanIdMap.put(beanId, instance);
            beanClzMap.put(clz, instance);
        } else {
            if(!beanIdMap.get(beanId).getClass().getName().equals(className)) {
                throw new MyException("bean id [" + beanId + "] 已定义，class: " + beanIdMap.get(beanId).getClass().getName());
            }
        }
    }

    /**
     * 注入属性
     * @param clz
     * @param instance
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static void injectFields(Class<?> clz, Object instance) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Field[] fields = clz.getDeclaredFields();
        if(fields != null && fields.length > 0) {
            for(Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();

                if(field.isAnnotationPresent(Autowired.class)) {
                    //按名称注入
                    if(definitionIdClassMap.containsKey(fieldName)) {
                        createBean(fieldName, definitionIdClassMap.get(fieldName));
                        Object obj = beanIdMap.get(fieldName);
                        field.set(instance, obj);
                    } else {
                        //按类型注入
                        String fieldClassName = field.getType().getName();
                        Class<?> fieldClz = Class.forName(fieldClassName);

                        if(definitionClassIdListMap.containsKey(fieldClz)) {
                            List<String> beanIdList = definitionClassIdListMap.get(fieldClz);

                            setField(instance, field, fieldName, fieldClassName, beanIdList);
                        } else {
                            for(Class<?> cls : definitionClassIdListMap.keySet()) {
                                if(fieldClz.isAssignableFrom(cls)) {
                                    List<String> beanIdList = definitionClassIdListMap.get(cls);
                                    setField(instance, field, fieldName, cls.getName(), beanIdList);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setField(Object instance, Field field, String fieldName, String fieldClassName, List<String> beanIdList) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(beanIdList.contains(fieldName)) {
            createBean(fieldName, fieldClassName);
            Object obj = beanIdMap.get(fieldName);
            field.set(instance, obj);
        } else if(beanIdList.size() == 1) {
            String defineBeanId = beanIdList.get(0);
            createBean(defineBeanId, fieldClassName);
            Object obj = beanIdMap.get(defineBeanId);
            field.set(instance, obj);
        }
    }

    public static <T> T getBean(String beanId) {
        Object bean = beanIdMap.get(beanId);
        return (T) bean;
    }

    public static <T> T getBean(Class<T> clz) {
        Object bean = beanClzMap.get(clz);
        if(bean != null) {
            return (T) bean;
        }

        String className = clz.getName();

        if(definitionClassIdListMap.containsKey(clz)) {
            return getInstanceByClass(clz, className);
        } else {
            for(Class<?> cls : definitionClassIdListMap.keySet()) {
                if(clz.isAssignableFrom(cls)) {
                    return getInstanceByClass(cls, className);
                }
            }
        }
        return null;
    }

    private static <T> T getInstanceByClass(Class<?> clz, String className) {
        List<String> beanIdList = definitionClassIdListMap.get(clz);

        if(beanIdList.contains(className)) {
            Object obj = beanIdMap.get(className);
            return (T) obj;
        } else if (beanIdList.size() == 1) {
            String beanId = beanIdList.get(0);
            Object obj = beanIdMap.get(beanId);
            return (T) obj;
        }
        return null;
    }

    public static Object getBeanByClassName(String className) {
        List<String> beanIdList = definitionClassNameIdListMap.get(className);

        if(CollectionUtils.isNotEmpty(beanIdList)) {
            String beanId = beanIdList.get(0);
            return getBean(beanId);
        }
        return null;
    }

    public static void setProxyByClassName(String className, Object proxy) {
        List<String> beanIdList = definitionClassNameIdListMap.get(className);

        if(CollectionUtils.isNotEmpty(beanIdList)) {
            for(String beanId : beanIdList) {
                Object bean = getBean(beanId);

                beanIdMap.put(beanId, proxy);
                beanClzMap.put(bean.getClass(), proxy);
            }
        }
    }
}
