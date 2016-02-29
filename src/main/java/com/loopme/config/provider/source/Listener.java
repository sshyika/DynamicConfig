package com.loopme.config.provider.source;

import com.loopme.config.api.Configuration;

public interface Listener<T extends Configuration> {

    /**
     * Will be called when configuration is updated
     *
     * @param fresh - latest version of config
     */
    void onUpdate(T fresh);

}
