package com.loopme.config.provider.source;

import com.loopme.config.api.Configuration;

public abstract class ConfigurationSource<T extends Configuration> {

    protected Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public abstract T getCurrent();

}
