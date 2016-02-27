/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider;

public class ConfigurablePlaceholder {
    private Class type;

    public ConfigurablePlaceholder(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
