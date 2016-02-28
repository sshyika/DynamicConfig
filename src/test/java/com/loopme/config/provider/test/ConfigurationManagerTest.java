package com.loopme.config.provider.test;

import com.loopme.config.api.Configuration;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ConfigurationManagerTest {

    @Test
    public void configUpdates() {
        ApplicationContext context = new ClassPathXmlApplicationContext("ConfigurationManagerTestContext.xml");

        ConfigA configAv1 = context.getBean(ConfigA.class);
        ConfigB configBv1 = context.getBean(ConfigB.class);
        ConfigurableA configurableA = context.getBean(ConfigurableA.class);
        ConfigurableB configurableB = context.getBean(ConfigurableB.class);
        TestConfigurationSource<ConfigA> sourceA = (TestConfigurationSource<ConfigA>)context.getBean("sourceA");
        TestConfigurationSource<ConfigB> sourceB = (TestConfigurationSource<ConfigB>)context.getBean("sourceB");

        // initial state
        assertFalse(configurableA.getOld().isPresent());
        assertEquals(configAv1, configurableA.getFresh());
        assertFalse(configurableB.getOld().isPresent());
        assertEquals(configBv1, configurableB.getFresh());

        ConfigA configAv2 = new ConfigA();
        sourceA.accept(configAv2);

        // state after new ConfigA arrived
        assertEquals(configAv1, configurableA.getOld().get());
        assertEquals(configAv2, configurableA.getFresh());
        assertFalse(configurableB.getOld().isPresent());
        assertEquals(configBv1, configurableB.getFresh());

        ConfigB configBv2 = new ConfigB();
        sourceB.accept(configBv2);

        // state after new ConfigB arrived
        assertEquals(configAv1, configurableA.getOld().get());
        assertEquals(configAv2, configurableA.getFresh());
        assertEquals(configBv1, configurableB.getOld().get());
        assertEquals(configBv2, configurableB.getFresh());
    }


    public static class ConfigA implements Configuration {
    }

    public static class ConfigB implements Configuration {
    }

    public static class ConfigurableA extends TestConfigurable<ConfigA> {
    }

    public static class ConfigurableB extends TestConfigurable<ConfigB> {
    }

}
