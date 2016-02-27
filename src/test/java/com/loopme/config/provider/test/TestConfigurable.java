/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;

import java.util.Optional;

abstract class TestConfigurable<T extends Configuration> implements Configurable<T> {
    private Optional<T> old;
    private T fresh;

    @Override
    public void update(Optional<T> old, T fresh) {
        this.old = old;
        this.fresh = fresh;
    }


    public Optional<T> getOld() {
        return old;
    }

    public T getFresh() {
        return fresh;
    }

}
