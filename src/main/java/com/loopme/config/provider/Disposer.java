package com.loopme.config.provider;

import com.loopme.config.api.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Disposes unusable Configurables
 */
public class Disposer implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(Disposer.class);

    private ReferenceQueue<Configurable> readyToDispose = new ReferenceQueue<>();
    private Set<SoftReference> disposedObjects = new HashSet<>();

    private ExecutorService executor;
    private ApplicationContext context;


    public void init() {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SoftReference sofRef = ((SoftReference)readyToDispose.remove());
                utilizeBean(sofRef);
            } catch (InterruptedException e) {
                LOG.error("Interrupted disposer thread", e);
            }
        });
    }

    public void destroy() {
        executor.shutdownNow();
        for (SoftReference sofRef : disposedObjects) {
            utilizeBean(sofRef);
        }
    }

    /**
     * Disposes given bean (all corresponding lifecycle callbacks will be invoked)
     */
    public synchronized void dispose(Configurable bean) {
        // wrap bean to SoftReference, it will be destroyed when all threads finish to work with this instance
        disposedObjects.add(new SoftReference<>(bean, readyToDispose));
    }


    private synchronized void utilizeBean(SoftReference sofRef) {
        disposedObjects.remove(sofRef);
        Object bean = sofRef.get();
        if (bean != null) {
            ((ConfigurableApplicationContext)context).getBeanFactory().destroyBean(bean);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
