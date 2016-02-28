/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;

import java.util.Optional;

abstract class TestConfigurable<T extends Configuration> implements Configurable<T> {
    private T config;

    @Override
    public void accept(T config) {
        this.config = config;
    }

    public T getConfig() {
        return config;
    }
}
