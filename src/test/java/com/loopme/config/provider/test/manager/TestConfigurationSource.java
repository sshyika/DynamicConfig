/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test.manager;

import com.loopme.config.api.Configuration;
import com.loopme.config.provider.source.ConfigurationSource;

class TestConfigurationSource<T extends Configuration> extends ConfigurationSource {
    private T config;

    public TestConfigurationSource(T config) {
        this.config = config;
    }

    @Override
    public Configuration getCurrent() {
        return config;
    }

    public void accept(T config) {
        this.config = config;
        listener.onUpdate(config);
    }
}
