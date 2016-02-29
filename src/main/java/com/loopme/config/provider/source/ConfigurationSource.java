package com.loopme.config.provider.source;

import com.loopme.config.api.Configuration;

/**
 * Source of configurations of particular type
 */
public abstract class ConfigurationSource<T extends Configuration> {

    protected Listener<T> listener;

    /**
     * Registers listener which will receive notifications about config updates
     */
    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    /**
     * Returns latest version of configuration
     */
    public abstract T getCurrent();

}
