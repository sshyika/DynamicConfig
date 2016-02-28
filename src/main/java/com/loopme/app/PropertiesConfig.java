package com.loopme.app;

import com.loopme.config.api.Configuration;

import java.util.Properties;

public class PropertiesConfig implements Configuration {

    private Properties properties;

    public PropertiesConfig(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }
}
