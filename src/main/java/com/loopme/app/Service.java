package com.loopme.app;

import com.loopme.config.api.Configurable;

import java.util.Optional;

public class Service implements Configurable<Config> {

    @Override
    public void accept(Optional<Config> old, Config fresh) {

    }

    public void serve() {

    }

    private void doServe() {

    }
}
