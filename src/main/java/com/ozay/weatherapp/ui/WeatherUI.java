package com.ozay.weatherapp.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ozay.weatherapp.api.OpenWeatherApiClient;
import com.ozay.weatherapp.model.ForecastData;
import com.ozay.weatherapp.model.WeatherData;
import com.ozay.weatherapp.util.ConfigLoader;
import java.net.URL;

public class WeatherUI {
    private static final Logger logger = LoggerFactory.getLogger(WeatherUI.class);
    
    // UI Components
    private JFrame frame;
    private JTextField cityField;
    private JButton searchButton;
    private JLabel temperatureLabel, feelsLikeLabel, humidityLabel, windLabel, descriptionLabel, cityCountryLabel;
    
    // 5-Day Forecast Components
    private JPanel forecastPanel;
    private JLabel[] dayDateLabels = new JLabel[5];
    private JLabel[] dayTempLabels = new JLabel[5];
    private JLabel[] dayIconLabels = new JLabel[5];
    
    private OpenWeatherApiClient apiClient;
    private ConfigLoader configLoader;

    public WeatherUI() {
        configLoader = new ConfigLoader();
        String apiKey = configLoader.getApiKey();
        
        if (apiKey.isEmpty()) {
            showErrorAndExit("API anahtarını config.properties dosyasına eklemelisiniz!");
            return;
        }
        
        apiClient = new OpenWeatherApiClient(apiKey);
        initializeUI();
    }

    private void initializeUI() {
        createMainFrame();
        setupSearchPanel();
        setupResultsPanel();
        setupForecastPanel();
        performInitialSearch();
    }

    private void createMainFrame() {
        frame = new JFrame("Hava Durumu Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600); // Boyut artırıldı
        frame.setMinimumSize(new Dimension(600, 500));
    }

    private void setupSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        cityField = new JTextField(20);
        cityField.setText(configLoader.getDefaultCity());
        
        searchButton = new JButton("Ara");
        searchButton.addActionListener(this::searchWeather);
        
        searchPanel.add(new JLabel("Şehir: "));
        searchPanel.add(cityField);
        searchPanel.add(searchButton);
        frame.add(searchPanel, BorderLayout.NORTH);
    }

    private void setupResultsPanel() {
        JPanel resultsPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        cityCountryLabel = createStyledLabel("", Font.BOLD, 18);
        temperatureLabel = createStyledLabel("Sıcaklık: ", Font.PLAIN, 14);
        feelsLikeLabel = createStyledLabel("Hissedilen: ", Font.PLAIN, 14);
        humidityLabel = createStyledLabel("Nem: ", Font.PLAIN, 14);
        windLabel = createStyledLabel("Rüzgar: ", Font.PLAIN, 14);
        descriptionLabel = createStyledLabel("Durum: ", Font.PLAIN, 14);
        
        resultsPanel.add(cityCountryLabel);
        resultsPanel.add(temperatureLabel);
        resultsPanel.add(feelsLikeLabel);
        resultsPanel.add(humidityLabel);
        resultsPanel.add(windLabel);
        resultsPanel.add(descriptionLabel);
        
        frame.add(resultsPanel, BorderLayout.CENTER);
    }

    private void setupForecastPanel() {
        forecastPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        forecastPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 20, 20, 20),
            BorderFactory.createTitledBorder("5 Günlük Tahmin")
        ));

        for (int i = 0; i < 5; i++) {
            JPanel dayPanel = createDayPanel(i);
            forecastPanel.add(dayPanel);
        }
        
        frame.add(forecastPanel, BorderLayout.SOUTH);
    }

    private JPanel createDayPanel(int index) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        dayDateLabels[index] = createStyledLabel("", Font.BOLD, 12);
        dayTempLabels[index] = createStyledLabel("", Font.PLAIN, 14);
        dayIconLabels[index] = new JLabel("", SwingConstants.CENTER);
        dayIconLabels[index].setPreferredSize(new Dimension(60, 60));
        
        panel.add(dayDateLabels[index], BorderLayout.NORTH);
        panel.add(dayIconLabels[index], BorderLayout.CENTER);
        panel.add(dayTempLabels[index], BorderLayout.SOUTH);
        
        return panel;
    }

    private JLabel createStyledLabel(String text, int style, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", style, size));
        return label;
    }

    private void performInitialSearch() {
        if (!cityField.getText().isEmpty()) {
            searchWeather(null);
        }
    }

    private void searchWeather(ActionEvent e) {
        final String city = cityField.getText().trim();
        
        if (city.isEmpty()) {
            showError("Lütfen bir şehir adı girin");
            return;
        }
        
        updateUIState(false);
        
        new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                WeatherData data = apiClient.getWeatherByCity(city);
                data.setForecast(apiClient.get5DayForecast(city));
                return data;
            }

            @Override
            protected void done() {
                try {
                    WeatherData data = get();
                    updateCurrentWeather(data);
                    updateForecast(data.getForecast());
                } catch (Exception ex) {
                    handleError(ex);
                } finally {
                    updateUIState(true);
                }
            }
        }.execute();
    }

    private void updateCurrentWeather(WeatherData data) {
        cityCountryLabel.setText(data.getCityName() + ", " + data.getCountry());
        temperatureLabel.setText(String.format("Sıcaklık: %.1f°C", data.getTemperature()));
        feelsLikeLabel.setText(String.format("Hissedilen: %.1f°C", data.getFeelsLike()));
        humidityLabel.setText(String.format("Nem: %.0f%%", data.getHumidity()));
        windLabel.setText(String.format("Rüzgar: %.1f m/s", data.getWindSpeed()));
        descriptionLabel.setText("Durum: " + data.getDescription());
    }

    private void updateForecast(List<ForecastData> forecast) {
        for (int i = 0; i < 5; i++) {
            if (i < forecast.size()) {
                ForecastData data = forecast.get(i);
                dayDateLabels[i].setText(data.getFormattedDate());
                dayTempLabels[i].setText(String.format("%.1f°C", data.getTemperature()));
                loadIconAsync(data.getIconCode(), dayIconLabels[i]);
            } else {
                clearForecastSlot(i);
            }
        }
    }

    private void loadIconAsync(String iconCode, JLabel target) {
        new Thread(() -> {
            try {
                ImageIcon icon = new ImageIcon(new URL(
                    "https://openweathermap.org/img/wn/" + iconCode + "@2x.png"
                ));
                SwingUtilities.invokeLater(() -> target.setIcon(icon));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> target.setIcon(null));
            }
        }).start();
    }

    private void clearForecastSlot(int index) {
        dayDateLabels[index].setText("--");
        dayTempLabels[index].setText("--");
        dayIconLabels[index].setIcon(null);
    }

    private void updateUIState(boolean enabled) {
        searchButton.setEnabled(enabled);
        cityCountryLabel.setText(enabled ? "" : "Yükleniyor...");
    }

    private void showErrorAndExit(String message) {
        logger.error(message);
        JOptionPane.showMessageDialog(null, message, "Hata", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private void handleError(Exception ex) {
        logger.error("Hata oluştu: {}", ex.getMessage());
        showError("Veri alınamadı: " + ex.getLocalizedMessage());
        clearForecast();
    }

    private void clearForecast() {
        for (int i = 0; i < 5; i++) {
            clearForecastSlot(i);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WeatherUI().frame.setVisible(true);
        });
    }
}