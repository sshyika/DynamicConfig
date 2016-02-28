package com.loopme.app.source;

import com.loopme.app.PropertiesConfig;
import com.loopme.config.provider.source.ConfigurationSource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PropertyFileConfigSource extends ConfigurationSource<PropertiesConfig> {

    private String fileName;
    private String directory;
    private PropertiesConfig current;
    private WatchService watcher;
    private ExecutorService executorService;


    public PropertyFileConfigSource(String fileName, Resource directory) throws IOException {
        this.fileName = fileName;
        this.directory = directory.getFile().getAbsolutePath();
    }


    public void init() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(directory);
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
               processFileUpdates();
            });
        } catch (IOException e) {
            throw new RuntimeException("Error watching file " + directory + System.lineSeparator() + fileName, e);
        }
    }

    public void destroy() {
        executorService.shutdown();
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


    private void processFileUpdates() {
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
