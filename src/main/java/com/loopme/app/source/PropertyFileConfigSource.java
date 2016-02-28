package com.loopme.app.source;

import com.loopme.app.PropertiesConfig;
import com.loopme.config.provider.source.ConfigurationSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

public class PropertyFileConfigSource extends ConfigurationSource<PropertiesConfig> {

    private String fileName;
    private String directory;
    private PropertiesConfig current;
    private WatchService watcher;


    public PropertyFileConfigSource(String fileName, String directory) {
        this.fileName = fileName;
        this.directory = directory;
    }


    public void init() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(directory);
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path file = ev.context();

                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY && file.toString().equals(fileName)) {
                        current = null;
                        if (listener != null) {
                            listener.onUpdate(getConfig());
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error watching file " + directory + System.lineSeparator() + fileName, e);
        }
    }

    public void destroy() {
        try {
            watcher.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing FS watcher", e);
        }
    }

    @Override
    public PropertiesConfig getCurrent() {
        return getConfig();
    }


    private PropertiesConfig getConfig() {
        if (current == null) {
            Properties props = new Properties();
            try (InputStream is = new FileInputStream(Paths.get(directory, fileName).toFile())) {
                props.load(is);
            } catch (Exception e) {
                throw new RuntimeException("Error reading file " + directory + System.lineSeparator() + fileName, e);
            }
            current = new PropertiesConfig(props);
        }
        return current;
    }
}
