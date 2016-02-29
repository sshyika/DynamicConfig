/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.app.source.test;

import com.loopme.app.PropertiesConfig;
import com.loopme.app.source.PropertyFileConfigSource;
import com.loopme.config.provider.source.Listener;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PropertyFileConfigSourceTest {
    private static final String FILE = "test.properties";

    private class CollectingListener implements Listener<PropertiesConfig> {
        PropertiesConfig config;

        @Override
        public void onUpdate(PropertiesConfig fresh) {
            config = fresh;
        }
    }


    @Test
    public void notifications() throws Exception {
        PropertyFileConfigSource source = new PropertyFileConfigSource(FILE, new ClassPathResource(""));
        source.init();
        try {
            CollectingListener listener = new CollectingListener();
            source.setListener(listener);

            writeToFile("testProp=a");

            Thread.sleep(200);

            assertEquals("a", listener.config.getProperties().getProperty("testProp"));

        } finally {
            source.destroy();
            writeToFile("");
        }
    }


    private void writeToFile(String line) throws Exception {
        Files.write(
                Paths.get(this.getClass().getClassLoader().getResource(FILE).toURI()),
                Arrays.asList(line),
                StandardOpenOption.WRITE
        );
    }


}
