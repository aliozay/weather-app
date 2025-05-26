package com.ozay.weatherapp.model;

import java.time.LocalDateTime;

public class HourlyForecast {
    private LocalDateTime time;
    private double temperature;
    private String iconCode;
    
    public HourlyForecast(LocalDateTime time, double temperature, String iconCode) {
        this.time = time;
        this.temperature = temperature;
        this.iconCode = iconCode;
    }

    // Getters
    public LocalDateTime getTime() { return time; }
    public double getTemperature() { return temperature; }
    public String getIconCode() { return iconCode; }
    
    public String getFormattedTime() {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}