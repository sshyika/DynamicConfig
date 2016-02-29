/*******************************************************************************
 * Copyright (c) 2015 PE INTERNATIONAL AG.
 * All rights reserved.
 *******************************************************************************/
package com.loopme.app.source.test;

import com.loopme.app.PropertiesConfig;
import com.loopme.app.source.PropertyFileConfigSource;
import com.loopme.config.provider.source.Listener;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PropertyFileConfigSourceTest {
    private final URL FILE = this.getClass().getClassLoader().getResource("test.properties");

    private class CollectingListener implements Listener<PropertiesConfig> {
        PropertiesConfig config;

        @Override
        public void onUpdate(PropertiesConfig fresh) {
            config = fresh;
        }
    }


    @Test
    public void notifications() throws Exception {
        PropertyFileConfigSource source = new PropertyFileConfigSource(Paths.get(FILE.toURI()).toString());
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
                Paths.get(FILE.toURI()),
                Arrays.asList(line),
                StandardOpenOption.WRITE
        );
    }


}
