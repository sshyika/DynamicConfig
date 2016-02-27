/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.config.provider.test;

import com.loopme.config.api.Configuration;
import com.loopme.config.provider.source.ConfigurationSource;
import org.junit.Test;

public class ConfigurableInterceptorTest {

    @Test
    public void updatesSynchronizationTest() {
        NewConfigurable configurable = new NewConfigurable();
        ConfigurationSource source = new TestConfigurationSource<TestConfig>(new TestConfig());

        //new ConfigurationManager(Arrays.asList(source), Arrays.asList(configurable));

        configurable.doNothing();
    }


    private class TestConfig implements Configuration {
    }

    public class NewConfigurable extends TestConfigurable<TestConfig> {

        @Override
        public Class<TestConfig> getConfigType() {
            return TestConfig.class;
        }

        public void doNothing() {

        }

    }

}
