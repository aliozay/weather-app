package com.ozay.weatherapp;

import com.ozay.weatherapp.ui.WeatherUI;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApp.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting JavaFX Weather App");
        WeatherUI weatherUI = new WeatherUI();
        weatherUI.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}