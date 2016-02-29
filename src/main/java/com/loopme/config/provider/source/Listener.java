package com.loopme.config.provider.source;

import com.loopme.config.api.Configuration;

public interface Listener {

    /**
     * Will be called when configuration is updated
     *
     * @param fresh - latest version of config
     */
    void onUpdate(Configuration fresh);

}
