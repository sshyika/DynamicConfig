package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class Disposer implements ApplicationContextAware {

    private ApplicationContext context;


    public void dispose(String name, Configurable bean) {
        // TODO: make sure that bean is not used by any thread
        ((ConfigurableApplicationContext)context).getBeanFactory().destroyBean(name, bean);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
