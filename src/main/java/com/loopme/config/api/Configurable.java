package com.loopme.config.api;

/**
 * All application components that depend on configuration of type T should implement this interface.
 * NOTE: all such components MUST BE declared as PROTOTYPE Spring bean
 */
public interface Configurable<T extends Configuration> {

    /**
     * Implementing class should configure itself by given configuration
     */
    void accept(T config);

}
