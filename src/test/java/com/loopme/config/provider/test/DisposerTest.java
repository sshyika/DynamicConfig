/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test;

import com.loopme.config.api.Configurable;
import com.loopme.config.api.Configuration;
import com.loopme.config.provider.Disposer;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class DisposerTest {
    private static volatile int currentValue;
    private static volatile boolean destroyed = false;

    public static class TestConfig implements Configuration {
    }

    public static class TestConfigurable implements Configurable<TestConfig> {
        private volatile int value = Integer.MIN_VALUE;

        public void destroy() {
            value = Integer.MAX_VALUE;
            destroyed = true;
        }

        @Override
        public void accept(TestConfig config) {
        }

        public void longRunningMethod() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            DisposerTest.currentValue = value;
        }
    }


    /**
     * Asserts that bean will not be disposed until all threads return from all methods of bean instance
     */
    @Test
    public void beansDisposing() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("DisposerTest.xml");

        TestConfigurationSource<TestConfig> source = (TestConfigurationSource<TestConfig>)context.getBean("source");
        TestConfigurable configurable = context.getBean(TestConfigurable.class);
        context.getBean(Disposer.class).setDisposeTimeout(1000);

        // assert that configurable has not been destroyed yet
        assertFalse(destroyed);

        // run long running method in parallel
        Thread thread = new Thread(configurable::longRunningMethod);
        thread.start();

        Thread.sleep(50);

        // refresh config before long running method is finished
        source.accept(new TestConfig());

        // wait while long running method is finished
        synchronized (thread) {
            thread.wait();
        }

        // assert that long running method finished before configurable been destroyed
        assertEquals(Integer.MIN_VALUE, currentValue);
        assertFalse(destroyed);

        Thread.sleep(1500);

        // assert that configurable eventually has been destroyed
        assertTrue(destroyed);
    }

}
