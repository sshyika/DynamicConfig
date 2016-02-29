package com.loopme.app.source;

import com.loopme.app.PropertiesConfig;
import com.loopme.config.provider.source.ConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

/**
 * Configuration source for configuration stored in properties file
 */
public class PropertyFileConfigSource extends ConfigurationSource<PropertiesConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(PropertyFileConfigSource.class);

    private final String fileName;
    private final String directory;
    private WatchService watcher;
    private TaskExecutor executor;
    private PropertiesConfig current;


    public PropertyFileConfigSource(String filePath, TaskExecutor executor) throws IOException {
        this.fileName = Paths.get(filePath).getFileName().toString();
        this.directory = Paths.get(filePath).getParent().toString();
        this.executor = executor;
    }


    /**
     * Creates WatchService and registers directory with it
     * Creates task which will poll WatchService
     */
    public void init() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(directory);
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            executor.execute(this::processFileUpdates);
        } catch (IOException e) {
            throw new RuntimeException("Error watching file " + getFileFullName(), e);
        }
        LOG.debug("Start watching updates of {}", getFileFullName());
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


    /**
     * Polls WatchService and for every update of the file of interest loads file content and notifies listener
     */
    private void processFileUpdates() {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException | ClosedWatchServiceException ex) {
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path file = ev.context();

                if (kind == StandardWatchEventKinds.ENTRY_MODIFY && file.toString().equals(fileName)) {
                    LOG.debug("File {} was updated", getFileFullName());

                    setCurrent(null);
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

    private synchronized PropertiesConfig getConfig() {
        if (current == null) {
            Properties props = new Properties();
            try (InputStream is = new FileInputStream(Paths.get(directory, fileName).toFile())) {
                props.load(is);
            } catch (Exception e) {
                throw new RuntimeException("Error reading file " + getFileFullName(), e);
            }
            current = new PropertiesConfig(props);
        }
        return current;
    }

    private synchronized void setCurrent(PropertiesConfig current) {
        this.current = current;
    }

    private String getFileFullName() {
        return directory + File.separator + fileName;
    }
}
