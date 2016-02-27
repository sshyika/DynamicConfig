/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test;

import com.loopme.config.api.Configuration;
import com.loopme.config.provider.source.ConfigurationSource;
import com.loopme.config.provider.source.Listener;

class TestConfigurationSource<T extends Configuration> implements ConfigurationSource {
    private T config;
    private Listener listener;

    public TestConfigurationSource(T config) {
        this.config = config;
    }

    @Override
    public Configuration getCurrent() {
        return config;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void accept(T config) {
        this.config = config;
        listener.onUpdate(config);
    }
}
