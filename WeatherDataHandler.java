package algo.weatherdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * Retrieves temperature data from a weather station file.
 * @author olivergottberg, sorenbeigi
 */
public class WeatherDataHandler {

	List<WeatherMeasurement> weatherData = new ArrayList<>(); //Skapa en ArrayList
	/**
	 * Load weather data from file.
	 *
	 * @param filePath path to file with weather data
	 * @throws IOException if there is a problem while reading the file
	 */
	public void loadData(String filePath) throws IOException {
		List<String> fileData = Files.readAllLines(Paths.get(filePath));

		for (String line : fileData) { //Loopa igenom varje rad
			String[] parts = line.split(";");
			LocalDate date = LocalDate.parse(parts[0]);
			double temperature = Double.parseDouble(parts[2]); //Hoppar från 0 direkt till 2 eftersom vi inte är intresserade av tiden
			boolean confirmed = parts[3].equals("G");

			WeatherMeasurement measurement = new WeatherMeasurement(date, temperature, confirmed);
			weatherData.add(measurement); //Skapa en measurement och lägg den i ArrayListen weatherData
		}
	}
	/**
	 * Search for average temperature for all dates between the two dates (inclusive).
	 * Result is sorted by date (ascending). When searching from 2000-01-01 to 2000-01-03
	 * the result should be:
	 * 2000-01-01 average temperature: 0.42 degrees Celsius
	 * 2000-01-02 average temperature: 2.26 degrees Celsius
	 * 2000-01-03 average temperature: 2.78 degrees Celsius
	 *
	 * @param dateFrom start date (YYYY-MM-DD) inclusive
	 * @param dateTo end date (YYYY-MM-DD) inclusive
	 * @return average temperature for each date, sorted by date
	 */
	public List<String> averageTemperatures(LocalDate dateFrom, LocalDate dateTo) {
		Map<LocalDate, List<Double>> temperatureMap = new HashMap<>(); //Skapa en HashMap för att koppla nycklar till värden

		for (WeatherMeasurement measurement : weatherData) {
			LocalDate date = measurement.getDate();
			if (date.isAfter(dateTo) || date.isBefore(dateFrom)) {
				continue; //Filtrera genom ArrayListen och exkludera datum som inte ligger inom rätt intervall
			}
			double temperature = measurement.getTemperature();
			temperatureMap.computeIfAbsent(date, k -> new ArrayList<>()).add(temperature); //Datumet är nyckeln och värdena är temperaturmätningarna som läggs i en arraylist, finns det ingen så skapas en
		}

		List<String> result = new ArrayList<>(); //Skapar en ArrayList för resultatet
		for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) { //Loopa igenom alla dagar
			List<Double> temperatures = temperatureMap.getOrDefault(date, new ArrayList<>()); //Hämta temperaturer, finns det inget en viss dag skickas en tom lista tillbaka för att undvika exceptions
			if (!temperatures.isEmpty()) {
				double sum = 0;
				for (double temp : temperatures) { //For-loop som går igenom temperaturmätningar, kan dock inte bli mer än 24 så därför inte n^2
					sum += temp;
				}
				double average = sum / temperatures.size();
				average = Math.round(average * 100.0) / 100.0;
				result.add(date + " average temperature: " + average + " degrees Celsius");
			}
		}
		return result;
	}
	/**
	 * Search for missing values between the two dates (inclusive) assuming there
	 * should be 24 measurement values for each day (once every hour). Result is
	 * sorted by number of missing values (descending). When searching from
	 * 2000-01-01 to 2000-01-03 the result should be:
	 * 2000-01-02 missing 1 values
	 * 2000-01-03 missing 1 values
	 * 2000-01-01 missing 0 values
	 *
	 * @param dateFrom start date (YYYY-MM-DD) inclusive
	 * @param dateTo end date (YYYY-MM-DD) inclusive
	 * @return dates with missing values together with number of missing values for each date, sorted by number of missing values (descending)
	 */
	public List<String> missingValues(LocalDate dateFrom, LocalDate dateTo) {
		Map<Object, Integer> missingValuesMap = new HashMap<>();

		for (WeatherMeasurement measurement : weatherData) {
			LocalDate date = measurement.getDate();
			if (date.isAfter(dateTo) || date.isBefore(dateFrom)) { //Filtrera och exkludera datum utanför intervallet
				continue;
			}
			missingValuesMap.put(date, missingValuesMap.getOrDefault(date, 0) + 1); //Mappar upp datum mot antalet mätningar per dag
		}
		List<String> missingValuesList = new ArrayList<>();
		for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) { //Loopa igenom dagar
			int missing = 24 - missingValuesMap.getOrDefault(date, 0); //Subtrahera antalet mätvärden från 24 som motsvarar en full dag
			missingValuesList.add(date + " missing " + missing + " values");
		}

		Collections.sort(missingValuesList, (a, b) -> { //Sortera missingValuesList genom att jämföra "missing" värdena
			String[] partsA = a.split(" ");
			String[] partsB = b.split(" ");
			int missingA = Integer.parseInt(partsA[partsA.length - 2]); //"missing" värdet finns på index length-2
			int missingB = Integer.parseInt(partsB[partsB.length - 2]);
			return Integer.compare(missingB, missingA);
		});

		return missingValuesList;
	}
	/**
	 * Search for percentage of approved values between the two dates (inclusive).
	 * When searching from 2000-01-01 to 2000-01-03 the result should be:
	 * Approved values between 2000-01-01 and 2000-01-03: 32.86 %
	 *
	 * @param dateFrom start date (YYYY-MM-DD) inclusive
	 * @param dateTo end date (YYYY-MM-DD) inclusive
	 * @return period and percentage of approved values for the period
	 */
	public List<String> approvedValues(LocalDate dateFrom, LocalDate dateTo) {
		int totalApprovedValuesCount = 0;
		int totalValuesCount = 0;

		for (WeatherMeasurement measurement : weatherData) { //Filtrera datumen på samma sätt som tidigare
			LocalDate date = measurement.getDate();
			if (date.isAfter(dateTo) || date.isBefore(dateFrom)) {
				continue;
			}

			totalValuesCount++;
			if (measurement.isConfirmed()) { //Är mätvärdet bekräftat så räkna upp ApprovedValuesCount
				totalApprovedValuesCount++;
			}
		}

		List<String> result = new ArrayList<>();
		if (totalValuesCount > 0) { //Räkna ut godkända värden i %
			double percentage = (double) totalApprovedValuesCount / totalValuesCount * 100;
			percentage = Math.round(percentage * 100.0) / 100.0;

			result.add("Approved values between " + dateFrom + " and " + dateTo + ": " + percentage + " %");
		} else {
			result.add("No values available for the specified date range.");
		}
		return result;
	}
}