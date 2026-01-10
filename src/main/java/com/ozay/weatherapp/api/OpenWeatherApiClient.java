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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.ozay.weatherapp.model.ForecastData;

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
    
    public List<ForecastData> get5DayForecast(String city) throws IOException {
        String url = String.format(
            "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&cnt=40",
            city,
            apiKey
        );

        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API Hatası: " + response.code());
            }

            String jsonData = response.body().string();
            Map<String, Object> responseMap = mapper.readValue(jsonData, Map.class);
            
            return processForecastItems((List<Map<String, Object>>) responseMap.get("list"));
        }
    }

    // YARDIMCI METOTLARI EKLEYİN
    private List<ForecastData> processForecastItems(List<Map<String, Object>> items) {
        List<ForecastData> forecastList = new ArrayList<>();
        
        for (Map<String, Object> item : items) {
            String dtTxt = (String) item.get("dt_txt");
            if (dtTxt.contains("12:00:00")) { // Sadece gündüz 12:00 verilerini al
                forecastList.add(parseForecastItem(item));
            }
        }
        
        return forecastList.size() > 5 ? forecastList.subList(0, 5) : forecastList;
    }

    private ForecastData parseForecastItem(Map<String, Object> item) {
        // Tarih dönüşümü
        String dateStr = ((String) item.get("dt_txt")).replace(" ", "T");
        LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Ana veriler
        Map<String, Object> main = (Map<String, Object>) item.get("main");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) item.get("weather");
        Map<String, Object> weather = weatherList.get(0);

        return new ForecastData(
            date,
            ((Number) main.get("temp")).doubleValue(),
            ((Number) main.get("feels_like")).doubleValue(),
            ((Number) main.get("humidity")).doubleValue(),
            (String) weather.get("description"),
            (String) weather.get("icon")
        );
    }  
    
}