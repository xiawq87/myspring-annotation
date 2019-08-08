package com.xwq.aspect;

import com.xwq.annotation.Aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Aspect("com.xwq.service.impl.UserServiceImpl")
public class LogInvocationHandler implements InvocationHandler {
    private Object target;

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("===> log start class[" + target.getClass().getName() + "] - method[" + method.getName() + "]");
        Object ret = method.invoke(target, args);
        System.out.println("===> log end class[" + target.getClass().getName() + "] - method[" + method.getName() + "], result: " + ret);
        return ret;
    }
}
