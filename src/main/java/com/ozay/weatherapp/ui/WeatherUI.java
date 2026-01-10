package com.ozay.weatherapp.ui;

import com.ozay.weatherapp.api.OpenWeatherApiClient;
import com.ozay.weatherapp.model.ForecastData;
import com.ozay.weatherapp.model.WeatherData;
import com.ozay.weatherapp.util.ConfigLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WeatherUI {
    private static final Logger logger = LoggerFactory.getLogger(WeatherUI.class);

    private OpenWeatherApiClient apiClient;
    private ConfigLoader configLoader;

    // UI Components
    private TextField cityField;
    private Label cityNameLabel;
    private Label temperatureLabel;
    private Label descriptionLabel;
    private Label humidityLabel;
    private Label windLabel;
    private Label feelsLikeLabel;
    private ImageView weatherIcon;
    private HBox forecastBox;
    private ProgressIndicator loadingIndicator;
    private Button searchButton;

    public WeatherUI() {
        configLoader = new ConfigLoader();
        String apiKey = configLoader.getApiKey();
        if (apiKey.isEmpty()) {
            logger.error("API Key missing!");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "API anahtarını config.properties dosyasına eklemelisiniz!");
                alert.showAndWait();
                System.exit(1);
            });
        }
        apiClient = new OpenWeatherApiClient(apiKey);
    }

    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.getStyleClass().add("main-container");
        root.setAlignment(Pos.TOP_CENTER);

        // Search Bar
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER);
        cityField = new TextField(configLoader.getDefaultCity());
        cityField.getStyleClass().add("search-field");
        cityField.setPromptText("Şehir adı girin...");
        
        searchButton = new Button("Hava Durumunu Al");
        searchButton.getStyleClass().add("search-button");
        searchButton.setOnAction(e -> fetchWeather());
        
        searchBar.getChildren().addAll(cityField, searchButton);

        // Main Weather Info
        VBox mainInfo = new VBox(10);
        mainInfo.getStyleClass().add("glass-pane");
        mainInfo.setAlignment(Pos.CENTER);
        mainInfo.setMaxWidth(400);

        cityNameLabel = new Label();
        cityNameLabel.getStyleClass().add("title-label");

        weatherIcon = new ImageView();
        weatherIcon.setFitWidth(100);
        weatherIcon.setFitHeight(100);

        temperatureLabel = new Label();
        temperatureLabel.getStyleClass().add("temp-label");

        descriptionLabel = new Label();
        descriptionLabel.getStyleClass().add("info-label");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setAlignment(Pos.CENTER);
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(5);

        feelsLikeLabel = new Label();
        feelsLikeLabel.getStyleClass().add("info-label");
        humidityLabel = new Label();
        humidityLabel.getStyleClass().add("info-label");
        windLabel = new Label();
        windLabel.getStyleClass().add("info-label");

        detailsGrid.add(new Label("Hissedilen:"), 0, 0);
        detailsGrid.add(feelsLikeLabel, 1, 0);
        detailsGrid.add(new Label("Nem:"), 0, 1);
        detailsGrid.add(humidityLabel, 1, 1);
        detailsGrid.add(new Label("Rüzgar:"), 0, 2);
        detailsGrid.add(windLabel, 1, 2);

        mainInfo.getChildren().addAll(cityNameLabel, weatherIcon, temperatureLabel, descriptionLabel, detailsGrid);

        // Forecast Area
        forecastBox = new HBox(15);
        forecastBox.setAlignment(Pos.CENTER);
        forecastBox.setPadding(new Insets(10));

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(50, 50);

        root.getChildren().addAll(searchBar, mainInfo, forecastBox, loadingIndicator);

        Scene scene = new Scene(root, 700, 750);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        primaryStage.setTitle("Hava Durumu Uygulaması");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial fetch
        fetchWeather();
    }

    private void fetchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) return;

        setLoading(true);

        CompletableFuture.runAsync(() -> {
            try {
                WeatherData weather = apiClient.getWeatherByCity(city);
                List<ForecastData> forecast = apiClient.get5DayForecast(city);
                
                Platform.runLater(() -> {
                    updateUI(weather, forecast);
                    setLoading(false);
                });
            } catch (Exception e) {
                logger.error("Error fetching weather", e);
                Platform.runLater(() -> {
                    setLoading(false);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Hava durumu bilgisi alınamadı: " + e.getMessage());
                    alert.show();
                });
            }
        });
    }

    private void updateUI(WeatherData weather, List<ForecastData> forecast) {
        cityNameLabel.setText(weather.getCityName() + ", " + weather.getCountry());
        temperatureLabel.setText(String.format("%.1f°C", weather.getTemperature()));
        descriptionLabel.setText(weather.getDescription().toUpperCase());
        feelsLikeLabel.setText(String.format("%.1f°C", weather.getFeelsLike()));
        humidityLabel.setText(String.format("%.0f%%", weather.getHumidity()));
        windLabel.setText(String.format("%.1f m/s", weather.getWindSpeed()));
        
        String iconUrl = "https://openweathermap.org/img/wn/" + weather.getIconCode() + "@4x.png";
        weatherIcon.setImage(new Image(iconUrl, true));

        forecastBox.getChildren().clear();
        for (ForecastData day : forecast) {
            VBox card = new VBox(5);
            card.getStyleClass().add("forecast-card");
            
            Label dateL = new Label(day.getFormattedDate().substring(0, 6)); // Short date
            dateL.getStyleClass().add("forecast-date");
            
            ImageView icon = new ImageView(new Image("https://openweathermap.org/img/wn/" + day.getIconCode() + ".png", true));
            
            Label tempL = new Label(String.format("%.0f°C", day.getTemperature()));
            tempL.getStyleClass().add("forecast-temp");
            
            card.getChildren().addAll(dateL, icon, tempL);
            forecastBox.getChildren().add(card);
        }
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        searchButton.setDisable(loading);
        forecastBox.setOpacity(loading ? 0.5 : 1.0);
    }
}