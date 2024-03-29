package com.example.irisassistant;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;


public class WeatherService {

    public interface WeatherServiceCallback {
        void onWeatherInfoReceived(String weatherInfo);
        void onError(String message);
    }

    private static final String API_KEY = "3cffe9e6b4d64b74739adbe0d14719e2"; // Replace with your actual API key
    private final OkHttpClient client;

    public WeatherService() {
        this.client = new OkHttpClient();
    }

    public void fetchWeatherData(String location, final WeatherServiceCallback callback) {
        // Ensure your API key and URL are correct. Handle URL encoding for the location query.
        String url = "https://api.openweathermap.org/data/2.5/weather?q=Settat&appid=" + API_KEY + "&units=metric";
        Log.d("WeatherService", "Fetching weather for: " + location);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "IrisAssistant App")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("WeatherService", "Network error", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Network error. Please check your connection."));

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("WeatherService", "Error fetching weather data: " + response.code());
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error fetching weather data. Please try again later."));
                    response.close();
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    String weatherInfo = parseWeatherInfo(jsonObject);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onWeatherInfoReceived(weatherInfo));
                } catch (JSONException e) {
                    Log.e("WeatherService", "Error processing the response", e);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error processing weather data."));
                } finally {
                    response.close();
                }
            }
        });
    }

    private String parseWeatherInfo(JSONObject jsonResponse) throws JSONException {
        double temperature = jsonResponse.getJSONObject("main").getDouble("temp"); // Temperature in Celsius
        String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
        return "Temperature: " + temperature + "°C, " + weatherDescription;

    }
}
