package com.ozay.weatherapp.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ozay.weatherapp.api.OpenWeatherApiClient;
import com.ozay.weatherapp.model.WeatherData;
import com.ozay.weatherapp.util.ConfigLoader;

import java.util.List;
import javax.swing.ImageIcon;
import java.net.URL;
import com.ozay.weatherapp.model.HourlyForecast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;   // Collectors için
import java.time.LocalDateTime;     // OpenWeatherApiClient içindeki LocalDateTime için
import java.time.format.DateTimeFormatter; // Tarih formatlama için

import com.ozay.weatherapp.model.HourlyForecast;  // HourlyForecast sınıfı için


public class WeatherUI {
    private static final Logger logger = LoggerFactory.getLogger(WeatherUI.class);
    
    private JFrame frame;
    private JTextField cityField;
    private JButton searchButton;
    private JLabel temperatureLabel;
    private JLabel feelsLikeLabel;
    private JLabel humidityLabel;
    private JLabel windLabel;
    private JLabel descriptionLabel;
    private JLabel cityCountryLabel;
    private JPanel hourlyPanel;
    private JLabel[] hourLabels = new JLabel[8];
    private JLabel[] hourlyIconLabels = new JLabel[8];
    private JLabel[] hourlyTempLabels = new JLabel[8];
    
    private OpenWeatherApiClient apiClient;
    private ConfigLoader configLoader;
    
    public WeatherUI() {
        configLoader = new ConfigLoader();
        String apiKey = configLoader.getApiKey();
        
        if (apiKey.isEmpty()) {
            logger.error("API anahtarı config dosyasında bulunamadı!");
            JOptionPane.showMessageDialog(null, 
                    "API anahtarını config.properties dosyasına eklemelisiniz!", 
                    "API Anahtarı Eksik", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        apiClient = new OpenWeatherApiClient(apiKey);
        createAndShowGUI();
    }
    
    private void createAndShowGUI() {
        // Ana pencere oluşturma
        frame = new JFrame("Hava Durumu Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500); // biraz daha büyük, çünkü saatlik verileri de göstereceğiz

        // Arama paneli
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cityField = new JTextField(20);
        cityField.setText(configLoader.getDefaultCity());
        searchButton = new JButton("Ara");
        searchButton.addActionListener(this::searchWeather);
        searchPanel.add(new JLabel("Şehir: "));
        searchPanel.add(cityField);
        searchPanel.add(searchButton);

        // Sonuçlar paneli
        JPanel resultsPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cityCountryLabel = new JLabel("Şehir, Ülke");
        cityCountryLabel.setFont(new Font("Arial", Font.BOLD, 18));

        temperatureLabel = new JLabel("Sıcaklık: ");
        feelsLikeLabel = new JLabel("Hissedilen: ");
        humidityLabel = new JLabel("Nem: ");
        windLabel = new JLabel("Rüzgar: ");
        descriptionLabel = new JLabel("Durum: ");

        resultsPanel.add(cityCountryLabel);
        resultsPanel.add(temperatureLabel);
        resultsPanel.add(feelsLikeLabel);
        resultsPanel.add(humidityLabel);
        resultsPanel.add(windLabel);
        resultsPanel.add(descriptionLabel);

        // Saatlik hava durumu paneli
        hourlyPanel = new JPanel(new GridLayout(1, 8, 5, 5)); // her biri 1 sütun olacak şekilde 8 adet
        for (int i = 0; i < 8; i++) {
            hourLabels[i] = new JLabel("", JLabel.CENTER);
            hourlyIconLabels[i] = new JLabel("", JLabel.CENTER);
            hourlyTempLabels[i] = new JLabel("", JLabel.CENTER);

            JPanel hourlyItem = new JPanel(new GridLayout(3, 1));
            hourlyItem.add(hourLabels[i]);
            hourlyItem.add(hourlyIconLabels[i]);
            hourlyItem.add(hourlyTempLabels[i]);
            hourlyPanel.add(hourlyItem);
        }

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);
        mainPanel.add(hourlyPanel, BorderLayout.SOUTH); // saatlik tahmini en alta ekliyoruz

        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // İlk arama
        SwingUtilities.invokeLater(this::performInitialSearch);
    }

    
    private void performInitialSearch() {
        if (!cityField.getText().isEmpty()) {
            searchWeather(null);
        }
    }
    
    private void searchWeather(ActionEvent e) {
        final String city = cityField.getText().trim();
        
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(frame, 
                    "Lütfen bir şehir adı girin", 
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // UI'ı güncelle
        searchButton.setEnabled(false);
        cityCountryLabel.setText("Yükleniyor...");
        
        // Arka planda API çağrısı yap
        new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                try {
                    return apiClient.getWeatherByCity(city);
                } catch (IOException ex) {
                    logger.error("Hava durumu verisi alınırken hata oluştu", ex);
                    throw ex;
                }
            }
            
            @Override
            protected void done() {
                try {
                    WeatherData data = get();
                    updateUI(data);
                } catch (Exception ex) {
                    logger.error("Hava durumu verisi işlenirken hata oluştu", ex);
                    JOptionPane.showMessageDialog(frame, 
                            "Hava durumu verileri alınamadı: " + ex,
                            "Hata", JOptionPane.ERROR_MESSAGE);
                    cityCountryLabel.setText("Hata oluştu");
                }
                searchButton.setEnabled(true);
            }
        }.execute();
    }
    
    private void updateUI(WeatherData data) {
        cityCountryLabel.setText(data.getCityName() + ", " + data.getCountry());
        temperatureLabel.setText("Sıcaklık: " + String.format("%.1f°C", data.getTemperature()));
        feelsLikeLabel.setText("Hissedilen: " + String.format("%.1f°C", data.getFeelsLike()));
        humidityLabel.setText("Nem: " + String.format("%.0f%%", data.getHumidity()));
        windLabel.setText("Rüzgar: " + String.format("%.1f m/s", data.getWindSpeed()));
        descriptionLabel.setText("Durum: " + data.getDescription());
        
        
        for (int i = 0; i < 8; i++) {
            hourLabels[i].setText("");
            hourlyIconLabels[i].setIcon(null);
            hourlyTempLabels[i].setText("");
            
            
    }
        
        new SwingWorker<List<HourlyForecast>, Void>() {
            @Override
            protected List<HourlyForecast> doInBackground() throws Exception {
                return apiClient.getHourlyForecast(data.getCityName());
            }

            @Override
            protected void done() {
                try {
                    List<HourlyForecast> hourlyList = get();

                    for (int i = 0; i < hourlyList.size(); i++) {
                        HourlyForecast hf = hourlyList.get(i);
                        hourLabels[i].setText(hf.getFormattedTime());
                        
                        // İkon URL'si (OpenWeather ikonu için)
                        String iconUrl = String.format("https://openweathermap.org/img/wn/%s.png", hf.getIconCode());
                        hourlyIconLabels[i].setIcon(new ImageIcon(new URL(iconUrl)));
                        
                        hourlyTempLabels[i].setText(String.format("%.1f°C", hf.getTemperature()));
                    }
                } catch (Exception e) {
                    logger.error("Saatlik hava durumu verisi yüklenirken hata oluştu", e);
                    // İstersen burada kullanıcıya da mesaj gösterilebilir
                }
            }
        }.execute();
    }
}