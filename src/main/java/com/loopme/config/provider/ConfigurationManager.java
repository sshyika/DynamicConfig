package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;
import com.loopme.config.provider.source.ConfigurationSource;
import com.loopme.config.provider.source.Listener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

public class ConfigurationManager implements Listener, BeanPostProcessor, ApplicationContextAware{

    private Map<Class, Set<String>> configurables;
    private Map<Class, Pair<Configuration>> configs;
    private ApplicationContext applicationContext;


    public ConfigurationManager(List<ConfigurationSource> sources) {
        configurables = new HashMap<>();
        configs = new HashMap<>(sources.size());
        initConfigs(sources);
    }


    @Override
    public synchronized void onUpdate(Configuration fresh) {
        Class configType = fresh.getClass();
        Pair<Configuration> config = configs.get(configType);
        configs.put(configType, new Pair<>(config.fresh, fresh));
        for (String name : configurables.get(configType)) {
            applicationContext.getBean(name, Configurable.class);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        if (bean instanceof Configurable) {
            Configurable configurable = (Configurable)bean;
            Class configType = configurable.getConfigType();
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
            ConfigurableInterceptor.accept(configurable);
            return new ConfigurablePlaceholder(configurable.getClass());
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
