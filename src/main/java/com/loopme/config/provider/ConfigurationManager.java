package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;
import com.loopme.config.provider.source.ConfigurationSource;
import com.loopme.config.provider.source.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

/**
 * Central point of configuration management
 */
public class ConfigurationManager implements Listener, BeanPostProcessor, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);

    private Map<Class<? extends Configuration>, Set<String>> configurables = new HashMap<>();
    private Map<Class<? extends Configuration>, Configuration> configs = new HashMap<>();
    private Map<String, HotSwappableTargetSource> swappers = new HashMap<>();

    private Disposer disposer;
    private ApplicationContext applicationContext;


    public ConfigurationManager(Disposer disposer) {
        this.disposer = disposer;
    }


    /**
     * Initialization callback
     */
    public synchronized void init() {
        initConfigs(applicationContext.getBeansOfType(ConfigurationSource.class).values());
    }

    /**
     * When new version of configuration received updates it in cache,
     * retrieves all configurables depending on configuration and for each configurable:
     *  - asks application context to create new instance of bean
     *  - swaps old bean by new, from this moment calls to configurable will be directed to new instance
     *  - send old instance of configurable to disposer
     */
    @Override
    public synchronized void onUpdate(Configuration fresh) {
        Class<? extends Configuration> configType = fresh.getClass();
        configs.put(configType, fresh);

        LOG.debug("New version of {} received", configType);

        Set<String> beanNames = configurables.get(configType);
        if (beanNames != null) {
            for (String name : beanNames) {
                Configurable bean = applicationContext.getBean(name, Configurable.class);
                Object old = swappers.get(name).swap(bean);
                disposer.dispose((Configurable)old);

                LOG.debug("Updated {} configurable bean", name);
            }
        }
    }

    /**
     * For every newly created Configurable saves it name,
     * feeds it by latest version of configuration,
     * wraps it in proxy if not already wrapped
     */
    @Override
    public synchronized Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        if (bean instanceof Configurable) {
            Configurable configurable = (Configurable)bean;
            Class<? extends Configuration> configType = ReflectionUtils.getConfigType(configurable);
            Set<String> dependents = configurables.get(configType);
            if (dependents == null) {
                dependents = new HashSet<>();
                configurables.put(configType, dependents);
            }
            dependents.add(name);
            Configuration config = configs.get(configType);
            if (config == null) {
                throw new IllegalStateException("ConfigurationSource for " + configType + " is not found");
            }
            LOG.debug("Detected configurable bean {} depending on {}", name, config.getClass());

            configurable.accept(config);
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


    /**
     * Wrap given bean to swapper, caches swapper and then makes proxy from it
     */
    private Object proxy(String name, Configurable bean) {
        if (!swappers.containsKey(name)) {
            HotSwappableTargetSource swapper = new HotSwappableTargetSource(bean);
            swappers.put(name, swapper);
            return ProxyFactory.getProxy(swapper);
        } else {
            return bean;
        }
    }

    /**
     * For every configuration source from given collection registers itself as its listener,
     * gets current configuration and caches it
     */
    private void initConfigs(Collection<ConfigurationSource> sources) {
        Set<Class> sourcesSet = new HashSet<>(sources.size());

        for (ConfigurationSource source : sources) {
            Configuration config = source.getCurrent();
            if (!sourcesSet.add(config.getClass())) {
                throw new IllegalArgumentException("Found more than one ConfigurationSource for " + config.getClass());
            }
            configs.put(config.getClass(), config);
            source.setListener(this);

            LOG.debug("Detected ConfigurationSource for {}", config.getClass());
        }
    }

}
