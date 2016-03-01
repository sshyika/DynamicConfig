/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.app;

import com.loopme.config.api.Configurable;

/**
 * Keeps specific property value from PropertiesConfig
 */
public class Service implements Configurable<PropertiesConfig> {
    private String propertyName;
    private String propertyValue;


    public Service(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public void accept(PropertiesConfig config) {
        propertyValue = config.getProperties().getProperty(propertyName);
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public String toString() {
        return "Service of " + propertyName;
    }

}
