package algo.weatherdata;

import java.time.LocalDate;

/**
 * Class for weather measurements
 * @author olivergottberg, sorenbeigi
 */
public class WeatherMeasurement {
    private LocalDate date;
    private double temperature;
    private boolean confirmed;

    public WeatherMeasurement(LocalDate date, double temperature, boolean confirmed) {
        this.date = date;
        this.temperature = temperature;
        this.confirmed = confirmed;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getTemperature() {
        return temperature;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
