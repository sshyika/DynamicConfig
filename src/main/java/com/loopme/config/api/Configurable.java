package com.loopme.config.api;

import java.util.Optional;

public interface Configurable<T extends Configuration> {

    void accept(Optional<T> old, T fresh);

}
