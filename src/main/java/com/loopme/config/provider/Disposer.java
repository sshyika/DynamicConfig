package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Disposes unusable Configurables
 */
public class Disposer implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(Disposer.class);

    private long disposeTimeout = 60000; // default 60 seconds
    private ScheduledExecutorService executor;
    private ConfigurableApplicationContext context;


    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void destroy() {
        executor.shutdownNow();
    }

    /**
     * Disposes given bean (all corresponding lifecycle callbacks will be invoked)
     */
    public void dispose(String name, Configurable bean) {
        // dispose bean with delay because it can be in use by other threads at the moment
        executor.schedule(() -> {

            context.getBeanFactory().destroyBean(name, bean);

            LOG.debug("Configurable {} of type {} is disposed", name, bean.getClass());
        }, disposeTimeout, TimeUnit.MILLISECONDS);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext)applicationContext;
    }

    public void setDisposeTimeout(long disposeTimeout) {
        this.disposeTimeout = disposeTimeout;
    }

}
