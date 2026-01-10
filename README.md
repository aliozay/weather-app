# SkyFX (JavaFX Modernized)

A sleek and modern weather application built with JavaFX, fetching real-time data from the OpenWeather API.

## Features

- **Real-time Weather**: Current temperature, feels like, humidity, and wind speed.
- **5-Day Forecast**: Visual forecast with weather icons.
- **Modern UI**: Dark theme with glassmorphism effects and responsive layout.
- **Performance**: Asynchronous data fetching to keep the UI responsive.
- **Error Handling**: Graceful error alerts for invalid cities or connection issues.

## Prerequisites

- Java 11 or higher
- Maven (optional, if building from source)
- OpenWeather API Key (configured in `config.properties`)

## Setup

1.  **API Key**: Obtain an API key from [OpenWeatherMap](https://openweathermap.org/api).
2.  **Configuration**: Open `src/main/resources/config.properties` and add your API key:
    ```properties
    api.key=YOUR_API_KEY_HERE
    default.city=Berlin
    ```

## Usage

### Running with Maven

```bash
mvn javafx:run
```

### Running the JAR

If you have a compiled JAR:

```bash
java -jar weather-app-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Screenshots

_(Screenshots will be added soon)_

## License

MIT
