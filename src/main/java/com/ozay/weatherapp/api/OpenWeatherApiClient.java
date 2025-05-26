package com.ozay.weatherapp.api;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozay.weatherapp.model.WeatherData;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;   // Collectors için
import java.time.LocalDateTime;     // OpenWeatherApiClient içindeki LocalDateTime için
import java.time.format.DateTimeFormatter; // Tarih formatlama için

import com.ozay.weatherapp.model.HourlyForecast;  // HourlyForecast sınıfı için


public class OpenWeatherApiClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherApiClient.class);
    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    
    public OpenWeatherApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }
    
    public WeatherData getWeatherByCity(String city) throws IOException {
        String url = String.format("%s?q=%s&appid=%s&units=metric", API_BASE_URL, city, apiKey);
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API yanıtı başarısız: " + response);
            }
            
            String responseBody = response.body().string();
            Map<String, Object> json = mapper.readValue(responseBody, Map.class);
            
            logger.info("API yanıt alındı: {}", responseBody);
            return WeatherData.fromJson(json);
        }
    }
    
    public WeatherData getWeatherByCoordinates(double lat, double lon) throws IOException {
        String url = String.format("%s?lat=%.6f&lon=%.6f&appid=%s&units=metric", 
                API_BASE_URL, lat, lon, apiKey);
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API yanıtı başarısız: " + response);
            }
            
            String responseBody = response.body().string();
            Map<String, Object> json = mapper.readValue(responseBody, Map.class);
            
            logger.info("API yanıt alındı: {}", responseBody);
            return WeatherData.fromJson(json);
        }
  
    
    }
    
    public List<HourlyForecast> getHourlyForecast(String city) throws IOException {
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric", city, apiKey);
        
        Request request = new Request.Builder().url(url).build();
        
        try (Response response = client.newCall(request).execute()) {
            String jsonData = response.body().string();
            Map<String, Object> responseMap = mapper.readValue(jsonData, Map.class);
            
            return ((List<Map<String, Object>>) responseMap.get("list")).stream()
                .limit(8) // Sonraki 8 saat (3 saatlik aralıklarla)
                .map(item -> {
                    Map<String, Object> main = (Map<String, Object>) item.get("main");
                    List<Map<String, Object>> weather = (List<Map<String, Object>>) item.get("weather");
                    return new HourlyForecast(
                        LocalDateTime.parse((String) item.get("dt_txt"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        ((Number) main.get("temp")).doubleValue(),
                        (String) weather.get(0).get("icon")
                    );
                })
                .collect(Collectors.toList());
        }
    }
    
    
}