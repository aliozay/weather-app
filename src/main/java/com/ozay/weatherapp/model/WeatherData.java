package com.ozay.weatherapp.model;

import java.util.List;
import java.util.Map;

public class WeatherData {
    private double temperature;
    private double feelsLike;
    private double humidity;
    private double windSpeed;
    private String description;
    private String iconCode;
    private String cityName;
    private String country;
    private List<ForecastData> forecast; // Yeni eklenen alan

    public WeatherData() {
    }

    public WeatherData(double temperature, double feelsLike, double humidity, 
                       double windSpeed, String description, String iconCode, 
                       String cityName, String country, List<ForecastData> forecast) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
        this.iconCode = iconCode;
        this.cityName = cityName;
        this.country = country;
        this.forecast = forecast;
    }

    // Getters ve Setters
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public List<ForecastData> getForecast() { return forecast; } // Yeni getter
    public void setForecast(List<ForecastData> forecast) { this.forecast = forecast; } // Yeni setter

    // Factory metodu (Değişmedi)
    public static WeatherData fromJson(Map<String, Object> json) {
        WeatherData data = new WeatherData();
        
        Map<String, Object> main = (Map<String, Object>) json.get("main");
        data.setTemperature(((Number) main.get("temp")).doubleValue());
        data.setFeelsLike(((Number) main.get("feels_like")).doubleValue());
        data.setHumidity(((Number) main.get("humidity")).doubleValue());
        
        Map<String, Object> wind = (Map<String, Object>) json.get("wind");
        data.setWindSpeed(((Number) wind.get("speed")).doubleValue());
        
        List<Map<String, Object>> weather = (List<Map<String, Object>>) json.get("weather");
        if (weather != null && !weather.isEmpty()) {
            Map<String, Object> weatherData = weather.get(0);
            data.setDescription((String) weatherData.get("description"));
            data.setIconCode((String) weatherData.get("icon"));
        }
        
        data.setCityName((String) json.get("name"));
        
        Map<String, Object> sys = (Map<String, Object>) json.get("sys");
        data.setCountry((String) sys.get("country"));
        
        return data;
    }

    @Override
    public String toString() {
        return String.format("%s, %s: %.1f°C, %s, Nem: %.0f%%, Rüzgar: %.1f m/s\nTahminler: %s", 
                cityName, country, temperature, description, humidity, windSpeed, forecast);
    }
}