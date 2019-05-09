
package org.n52.prosecco;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonFileReader {

    private final File configFile;

    public JsonFileReader(File configFile) {
        Objects.requireNonNull(configFile, "config file is null");
        if (! (configFile.exists() && configFile.canRead())) {
            String path = configFile.getAbsolutePath();
            throw new IllegalArgumentException("Can not access config file at " + path + " (does it exists?)");
        }
        this.configFile = configFile;
    }

    public <T> T readConfig(Class<T> configType) throws ConfigurationException {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(configFile, configType);
        } catch (IOException e) {
            String path = configFile.getAbsolutePath();
            throw new ConfigurationException("Can not read from config file: " + path, e);
        }
    }

}
