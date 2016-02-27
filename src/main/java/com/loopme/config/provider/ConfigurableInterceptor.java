package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Aspect
public class ConfigurableInterceptor {

    private static ConcurrentMap<Class, Configurable> targets = new ConcurrentHashMap<>();

    /**
     * Advice for every public method of classes implementing Configurable except methods declared in Configurable
     */
    @Around(
            "call(public * com.loopme.config.api.Configurable+.*(..)) && !call(* com.loopme.config.api.Configurable.*(..))"
    )
    public Object aroundEveryPublicMethod(ProceedingJoinPoint jp) throws Throwable {

        Object[] proceedArgs = new Object[jp.getArgs().length + 2];
        proceedArgs[0] = jp.getThis();
        proceedArgs[1] = targets.get(((ConfigurablePlaceholder)jp.getTarget()).getType());
        System.arraycopy(jp.getArgs(), 0, proceedArgs, 2, jp.getArgs().length);

        return jp.proceed(proceedArgs);
    }


    public static void accept(Configurable configurable) {
        targets.put(configurable.getClass(), configurable);
    }

}