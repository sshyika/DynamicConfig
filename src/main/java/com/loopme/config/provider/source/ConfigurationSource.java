package com.loopme.config.provider.source;

import com.loopme.config.api.Configuration;

public interface ConfigurationSource<T extends Configuration> {

    void setListener(Listener listener);

    T getCurrent();

}
