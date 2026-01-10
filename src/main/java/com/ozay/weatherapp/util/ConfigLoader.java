package com.ozay.weatherapp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "config.properties";
    
    private Properties properties;
    
    public ConfigLoader() {
        properties = new Properties();
        loadProperties();
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("Config dosyası bulunamadı: {}. Varsayılan değerler kullanılacak.", CONFIG_FILE);
                return;
            }
            
            properties.load(input);
            logger.info("Config dosyası yüklendi: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Config dosyası yüklenirken hata oluştu", e);
        }
    }
    
    public String getApiKey() {
        return properties.getProperty("api.key", "");
    }
    
    public String getDefaultCity() {
        return properties.getProperty("default.city", "Istanbul");
    }
}