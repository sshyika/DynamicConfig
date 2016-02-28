package com.loopme.config.provider;

import com.googlecode.gentyref.GenericTypeReflector;
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

import java.util.*;

public class ConfigurationManager implements Listener, BeanPostProcessor, ApplicationContextAware {

    private Map<Class<? extends Configuration>, Set<String>> configurables = new HashMap<>();
    private Map<Class<? extends Configuration>, Configuration> configs = new HashMap<>();
    private Map<String, HotSwappableTargetSource> swappers = new HashMap<>();

    private Disposer disposer;
    private ApplicationContext applicationContext;


    public ConfigurationManager(Disposer disposer) {
        this.disposer = disposer;
    }

    public void init() {
        initConfigs(applicationContext.getBeansOfType(ConfigurationSource.class).values());
    }


    @Override
    public synchronized void onUpdate(Configuration fresh) {
        Class<? extends Configuration> configType = fresh.getClass();
        configs.put(configType, fresh);
        for (String name : configurables.get(configType)) {
            Configurable bean = applicationContext.getBean(name, Configurable.class);
            Object old = swappers.get(name).swap(bean);
            disposer.dispose(name, (Configurable)old);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        if (bean instanceof Configurable) {
            Configurable configurable = (Configurable)bean;
            Class<? extends Configuration> configType = getConfigType(configurable);
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


    private Object proxy(String name, Configurable bean) {
        if (!swappers.containsKey(name)) {
            HotSwappableTargetSource swapper = new HotSwappableTargetSource(bean);
            swappers.put(name, swapper);
            return ProxyFactory.getProxy(swapper);
        } else {
            return bean;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Configuration> getConfigType(Configurable object) {
        return (Class<? extends Configuration>)GenericTypeReflector.getTypeParameter(object.getClass(), Configurable.class.getTypeParameters()[0]);
    }

    private void initConfigs(Collection<ConfigurationSource> sources) {
        Set<Class> sourcesSet = new HashSet<>(sources.size());

        for (ConfigurationSource source : sources) {
            Configuration config = source.getCurrent();
            if (!sourcesSet.add(config.getClass())) {
                throw new IllegalArgumentException("Found more than one ConfigurationSource for " + config.getClass());
            }
            source.setListener(this);
            configs.put(config.getClass(), config);
        }
    }

}
