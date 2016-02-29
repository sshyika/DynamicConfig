/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private TaskExecutor executor;
    private List<Service> services;
    private volatile boolean running = true;


    public Client(List<Service> services, TaskExecutor executor) {
        this.services = services;
        this.executor = executor;
    }


    public void init() {
        executor.execute(() -> {
            try {

                while (running) {
                    for (Service service : services) {
                        LOG.info("{}: {}", service, service.getPropertyValue());
                    }
                    Thread.sleep(1500);
                }

            } catch (InterruptedException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        });
    }

    public void destroy() {
        running = false;
    }

}
