package com.loopme.config.api;

import java.util.Optional;

public interface Configurable<T extends Configuration> {

    void update(Optional<T> old, T fresh);

    Class<T> getConfigType();

}
