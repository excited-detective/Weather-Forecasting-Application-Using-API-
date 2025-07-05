import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class weather {
    private static final String API_KEY = "afe69f0c4c055e3c55ee8415b7bf2494";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter city name: ");
        String city = scanner.nextLine().trim();
        scanner.close();
        
        if (city.isEmpty()) {
            System.out.println("Error: City name cannot be empty");
            return;
        }
        
        try {
            String encodedCity = URLEncoder.encode(city, "UTF-8");
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" 
                + encodedCity + "&appid=" + API_KEY + "&units=metric";
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                String jsonResponse = readResponse(conn);
                parseAndDisplayWeather(jsonResponse);
            } else {
                handleErrorResponse(responseCode, conn);
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }
    
    private static void parseAndDisplayWeather(String json) {
        try {
            String temp = extractValue(json, "\"temp\":", ",", "}");
            String humidity = extractValue(json, "\"humidity\":", ",", "}");
            String description = extractDescription(json);
            String cityName = extractValue(json, "\"name\":\"", "\"", "\"");
            
            if (!cityName.equals("N/A")) {
                System.out.println("\nWeather for " + cityName + ":");
            } else {
                System.out.println("\nWeather Information:");
            }
            
            System.out.println("Temperature: " + temp + "°C");
            System.out.println("Humidity: " + humidity + "%");
            System.out.println("Description: " + description);
            
        } catch (Exception e) {
            System.out.println("Error parsing weather data");
        }
    }
    
    private static String extractValue(String json, String startKey, String... endChars) {
        int start = json.indexOf(startKey);
        if (start == -1) return "N/A";
        start += startKey.length();
        
        int end = json.length();
        for (String endChar : endChars) {
            int tempEnd = json.indexOf(endChar, start);
            if (tempEnd != -1 && tempEnd < end) {
                end = tempEnd;
            }
        }
        
        if (end > start) {
            return json.substring(start, end).replace("\"", "").trim();
        }
        return "N/A";
    }
    
    private static String extractDescription(String json) {
        int weatherStart = json.indexOf("\"weather\":[");
        if (weatherStart == -1) return "N/A";
        
        int descStart = json.indexOf("\"description\":\"", weatherStart);
        if (descStart == -1) return "N/A";
        
        descStart += "\"description\":\"".length();
        int descEnd = json.indexOf("\"", descStart);
        
        if (descEnd != -1) {
            return json.substring(descStart, descEnd);
        }
        return "N/A";
    }
    
    private static void handleErrorResponse(int responseCode, HttpURLConnection conn) {
        String errorMessage;
        switch (responseCode) {
            case 401:
                errorMessage = "Invalid API key";
                break;
            case 404:
                errorMessage = "City not found";
                break;
            case 429:
                errorMessage = "Too many requests. Please try again later";
                break;
            default:
                errorMessage = "HTTP Error: " + responseCode;
        }
        System.out.println("Error: " + errorMessage);
    }
}
