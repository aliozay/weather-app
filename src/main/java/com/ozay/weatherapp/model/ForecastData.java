package com.ozay.weatherapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ForecastData {
    private LocalDateTime date;
    private double temperature;
    private double feelsLike;
    private double humidity;
    private String description;
    private String iconCode;

    public ForecastData(LocalDateTime date, double temperature, double feelsLike, 
                       double humidity, String description, String iconCode) {
        this.date = date;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.iconCode = iconCode;
    }

    // Tarih formatlama metodu
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM EEE HH:mm");
        return date.format(formatter);
    }

    // Getter metodları
    public LocalDateTime getDate() { return date; }
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public double getHumidity() { return humidity; }
    public String getDescription() { return description; }
    public String getIconCode() { return iconCode; }
}