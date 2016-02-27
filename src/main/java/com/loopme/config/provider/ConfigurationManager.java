package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;
import com.loopme.config.provider.source.ConfigurationSource;
import com.loopme.config.provider.source.Listener;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ConfigurationManager implements Listener, BeanPostProcessor, ApplicationContextAware {

    private Map<Class<? extends Configuration>, Set<String>> configurables;
    private Map<Class<? extends Configuration>, Pair<Configuration>> configs;
    private Map<String, HotSwappableTargetSource> swappers;
    private ApplicationContext applicationContext;


    public ConfigurationManager(List<ConfigurationSource> sources) {
        configurables = new HashMap<>();
        swappers = new HashMap<>();
        configs = new HashMap<>(sources.size());
        initConfigs(sources);
    }


    @Override
    public synchronized void onUpdate(Configuration fresh) {
        Class<? extends Configuration> configType = fresh.getClass();
        Pair<Configuration> config = configs.get(configType);
        configs.put(configType, new Pair<>(config.fresh, fresh));
        for (String name : configurables.get(configType)) {
            Configurable bean = applicationContext.getBean(name, Configurable.class);
            Object old = swappers.get(name).swap(bean);
            // TODO: destroy old instance
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        if (bean instanceof Configurable) {
            Configurable configurable = (Configurable)bean;
            Class<? extends Configuration> configType = configurable.getConfigType();
            Set<String> dependents = configurables.get(configType);
            if (dependents == null) {
                dependents = new HashSet<>();
                configurables.put(configType, dependents);
            }
            dependents.add(name);
            Pair<Configuration> config = configs.get(configType);
            if (config == null) {
                throw new IllegalStateException("ConfigurationSource for " + configType + " is not found");
            }
            configurable.update(config.old, config.fresh);
            return proxy(name, configurable);
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    private Object proxy(String name, Configurable bean) {
        if (!swappers.containsKey(name)) {
            HotSwappableTargetSource swapper = new HotSwappableTargetSource(bean);
            swappers.put(name, swapper);
            return ProxyFactory.getProxy(swapper);
        } else {
            return bean;
        }
    }

    private void initConfigs(List<ConfigurationSource> sources) {
        Set<Class> sourcesSet = new HashSet<>(sources.size());

        for (ConfigurationSource source : sources) {
            Configuration config = source.getCurrent();
            if (!sourcesSet.add(config.getClass())) {
                throw new IllegalArgumentException("Found more than one ConfigurationSource for " + config.getClass());
            }
            source.setListener(this);
            configs.put(config.getClass(), new Pair<>(null, config));
        }
    }


    private class Pair<T> {
        Optional<T> old;
        T fresh;

        private Pair(T old, T fresh) {
            this.old = old == null ? Optional.<T>empty() : Optional.of(old);
            this.fresh = fresh;
        }
    }

}
