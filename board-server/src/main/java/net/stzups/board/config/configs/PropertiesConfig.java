package net.stzups.board.config.configs;

import net.stzups.board.config.StringConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConfig implements StringConfig {
    private Properties properties;

    public PropertiesConfig(String name) throws IOException {
        properties = new Properties();
        File file = new File(name);
        if (file.exists()) {//load user defined config if created
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                properties.load(fileInputStream);
            }
        }
    }

    @Override
    public String get(String key) {
        return properties.getProperty(key);
    }
}
