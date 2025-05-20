package com.ozay.weatherapp;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ozay.weatherapp.ui.WeatherUI;

public class WeatherApp {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting the App");
        
        SwingUtilities.invokeLater(() -> {
            new WeatherUI();
            logger.info("UI Started");
        });
    }
}