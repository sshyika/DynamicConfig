package com.loopme.app;

import com.loopme.config.api.Configurable;

import java.util.Optional;

public class Service implements Configurable<Config> {

    @Override
    public void update(Optional<Config> old, Config fresh) {

    }

    @Override
    public Class<Config> getConfigType() {
        return Config.class;
    }

    public void serve() {

    }

    private void doServe() {

    }
}
