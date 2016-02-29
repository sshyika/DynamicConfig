/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider;

import com.googlecode.gentyref.GenericTypeReflector;
import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReflectionUtils {

    private static final ConcurrentMap<Class<? extends Configurable>, Class<? extends Configuration>> cache = new ConcurrentHashMap<>();


    /**
     * Returns type parameter (class implementing Configuration class) of given Configurable object
     * Uses caching to improve performance
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Configuration> getConfigType(Configurable object) {
        Class<? extends Configuration> configClass = cache.get(object.getClass());
        if (configClass == null) {
            configClass = (Class<? extends Configuration>) GenericTypeReflector.getTypeParameter(object.getClass(), Configurable.class.getTypeParameters()[0]);
            cache.put(object.getClass(), configClass);
        }
        return configClass;
    }

}
