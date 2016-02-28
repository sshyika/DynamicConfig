package com.loopme.config.api;

public interface Configurable<T extends Configuration> {

    void accept(T config);

}
