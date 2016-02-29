/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

abstract class TestConfigurable<T extends Configuration> implements Configurable<T>, InitializingBean, DisposableBean {
    public static int initCounter = 0;
    public static int destroyCounter = 0;
    private T config;

    @Override
    public void accept(T config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initCounter++;
    }

    @Override
    public void destroy() throws Exception {
        destroyCounter++;
    }

    public T getConfig() {
        return config;
    }
}
