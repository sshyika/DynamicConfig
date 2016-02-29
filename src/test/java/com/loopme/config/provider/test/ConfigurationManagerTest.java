package com.loopme.config.provider.test;

import com.loopme.config.api.Configuration;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;

public class ConfigurationManagerTest {


    public static class ConfigA implements Configuration {
    }

    public static class ConfigB implements Configuration {
    }

    public static class ConfigurableA extends TestConfigurable<ConfigA> {
    }

    public static class ConfigurableB extends TestConfigurable<ConfigB> {
    }


    @Test
    public void configurablesStateUpdates() {
        ApplicationContext context = new ClassPathXmlApplicationContext("ConfigurationManagerTestContext.xml");

        ConfigA configAv1 = context.getBean(ConfigA.class);
        ConfigB configBv1 = context.getBean(ConfigB.class);

        ConfigurableA configurableA = context.getBean(ConfigurableA.class);
        ConfigurableB configurableB = context.getBean(ConfigurableB.class);

        TestConfigurationSource<ConfigA> sourceA = (TestConfigurationSource<ConfigA>)context.getBean("sourceA");
        TestConfigurationSource<ConfigB> sourceB = (TestConfigurationSource<ConfigB>)context.getBean("sourceB");


        // assert initial state
        assertEquals(configAv1, configurableA.getConfig());
        assertEquals(configBv1, configurableB.getConfig());
        assertEquals(2, TestConfigurable.initCounter);
        assertEquals(0, TestConfigurable.destroyCounter);

        ConfigA configAv2 = new ConfigA();
        sourceA.accept(configAv2);

        // assert state after new ConfigA arrived
        assertEquals(configAv2, configurableA.getConfig());
        assertEquals(configBv1, configurableB.getConfig());
        assertEquals(3, TestConfigurable.initCounter);
        assertEquals(1, TestConfigurable.destroyCounter);

        ConfigB configBv2 = new ConfigB();
        sourceB.accept(configBv2);

        // assert state after new ConfigB arrived
        assertEquals(configAv2, configurableA.getConfig());
        assertEquals(configBv2, configurableB.getConfig());
        assertEquals(4, TestConfigurable.initCounter);
        assertEquals(2, TestConfigurable.destroyCounter);
    }


}
