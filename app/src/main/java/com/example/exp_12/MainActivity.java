package com.example.exp_12;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etCity;
    private Button btnSearch, btnRefresh;
    private ProgressBar progressBar;
    private TextView tvError, tvCityName, tvTemperature, tvCondition, tvFeelsLike, tvHumidity, tvWindSpeed, tvMinTemp, tvMaxTemp;
    private ImageView ivWeatherIcon, ivCityIllustration;
    private View cardWeather;

    private ExecutorService executorService;
    private String lastSearchedCity = "Bengaluru";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvMinTemp = findViewById(R.id.tvMinTemp);
        tvMaxTemp = findViewById(R.id.tvMaxTemp);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        ivCityIllustration = findViewById(R.id.ivCityIllustration);
        cardWeather = findViewById(R.id.cardWeather);

        executorService = Executors.newSingleThreadExecutor();

        // Initialize popular city quick chips
        findViewById(R.id.chipBengaluru).setOnClickListener(v -> loadCityWeather("Bengaluru"));
        findViewById(R.id.chipLondon).setOnClickListener(v -> loadCityWeather("London"));
        findViewById(R.id.chipNewYork).setOnClickListener(v -> loadCityWeather("New York"));
        findViewById(R.id.chipTokyo).setOnClickListener(v -> loadCityWeather("Tokyo"));
        findViewById(R.id.chipParis).setOnClickListener(v -> loadCityWeather("Paris"));
        findViewById(R.id.chipMumbai).setOnClickListener(v -> loadCityWeather("Mumbai"));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCity.getText().toString().trim();
                if (TextUtils.isEmpty(city)) {
                    etCity.setError(getString(R.string.error_empty_city));
                    return;
                }
                hideKeyboard();
                lastSearchedCity = city;
                fetchWeatherData(city);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(lastSearchedCity)) {
                    fetchWeatherData(lastSearchedCity);
                } else {
                    String city = etCity.getText().toString().trim();
                    if (!TextUtils.isEmpty(city)) {
                        lastSearchedCity = city;
                        fetchWeatherData(city);
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a city first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Load default initial city weather on app startup
        etCity.setText("Bengaluru");
        fetchWeatherData("Bengaluru");
    }

    private void loadCityWeather(String city) {
        etCity.setText(city);
        hideKeyboard();
        lastSearchedCity = city;
        fetchWeatherData(city);
    }

    private void fetchWeatherData(final String cityName) {
        if (!isNetworkAvailable()) {
            showError(getString(R.string.error_network));
            return;
        }

        showLoading(true);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Step 1: Geocoding API to get Latitude and Longitude (No API Key required)
                    String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + 
                            java.net.URLEncoder.encode(cityName, "UTF-8") + "&count=1";
                    
                    String geoJsonResponse = makeHttpGETRequest(geoUrlStr);
                    if (geoJsonResponse == null) {
                        showErrorOnUI(getString(R.string.error_network));
                        return;
                    }

                    JSONObject geoObj = new JSONObject(geoJsonResponse);
                    if (!geoObj.has("results")) {
                        showErrorOnUI(getString(R.string.error_city_not_found));
                        return;
                    }

                    JSONArray resultsArray = geoObj.getJSONArray("results");
                    JSONObject cityObj = resultsArray.getJSONObject(0);
                    double lat = cityObj.getDouble("latitude");
                    double lon = cityObj.getDouble("longitude");
                    String name = cityObj.getString("name");
                    String country = cityObj.has("country") ? cityObj.getString("country") : "";

                    // Step 2: Open-Meteo Weather Forecast API
                    String weatherUrlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + 
                            "&longitude=" + lon + 
                            "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" + 
                            "&daily=temperature_2m_max,temperature_2m_min&timezone=auto";

                    String weatherJsonResponse = makeHttpGETRequest(weatherUrlStr);
                    if (weatherJsonResponse == null) {
                        showErrorOnUI(getString(R.string.error_network));
                        return;
                    }

                    // Step 3: Download weather condition icon and city illustration bitmap from public APIs
                    int weatherCode = new JSONObject(weatherJsonResponse).getJSONObject("current").getInt("weather_code");
                    String iconCode = getWeatherIconCode(weatherCode);
                    String iconUrlStr = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
                    String illustrationUrlStr = "https://picsum.photos/seed/" + java.net.URLEncoder.encode(cityName, "UTF-8") + "/800/300";

                    final Bitmap weatherBitmap = downloadBitmapFromUrl(iconUrlStr);
                    final Bitmap illustrationBitmap = downloadBitmapFromUrl(illustrationUrlStr);

                    parseAndDisplayWeather(weatherJsonResponse, name, country, weatherBitmap, illustrationBitmap);

                } catch (final Exception e) {
                    showErrorOnUI(getString(R.string.error_network) + "\n(" + e.getMessage() + ")");
                }
            }
        });
    }

    private String makeHttpGETRequest(String requestUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            }
        } catch (Exception e) {
            // Log error
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private Bitmap downloadBitmapFromUrl(String urlString) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (Exception ignored) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void parseAndDisplayWeather(String jsonResponse, String cityName, String country, final Bitmap weatherBitmap, final Bitmap illustrationBitmap) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject currentObj = jsonObject.getJSONObject("current");
            JSONObject dailyObj = jsonObject.getJSONObject("daily");

            double temp = currentObj.getDouble("temperature_2m");
            double feelsLike = currentObj.getDouble("apparent_temperature");
            int humidity = currentObj.getInt("relative_humidity_2m");
            double windSpeed = currentObj.getDouble("wind_speed_10m");
            int weatherCode = currentObj.getInt("weather_code");

            JSONArray maxArr = dailyObj.getJSONArray("temperature_2m_max");
            JSONArray minArr = dailyObj.getJSONArray("temperature_2m_min");
            double tempMax = maxArr.getDouble(0);
            double tempMin = minArr.getDouble(0);

            final String displayCity = TextUtils.isEmpty(country) ? cityName : cityName + ", " + country;
            final String displayTemp = Math.round(temp) + "°C";
            final String displayCondition = getWeatherConditionText(weatherCode);
            final String displayFeelsLike = "Feels like: " + Math.round(feelsLike) + "°C";
            final String displayHumidity = humidity + "%";
            final String displayWind = Math.round(windSpeed) + " km/h";
            final String displayMinTemp = Math.round(tempMin) + "°C";
            final String displayMaxTemp = Math.round(tempMax) + "°C";
            final int weatherIconRes = getWeatherIconRes(weatherCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvCityName.setText(displayCity);
                    tvTemperature.setText(displayTemp);
                    tvCondition.setText(displayCondition);
                    tvFeelsLike.setText(displayFeelsLike);
                    tvHumidity.setText(displayHumidity);
                    tvWindSpeed.setText(displayWind);
                    tvMinTemp.setText(displayMinTemp);
                    tvMaxTemp.setText(displayMaxTemp);

                    if (weatherBitmap != null) {
                        ivWeatherIcon.setImageBitmap(weatherBitmap);
                    } else {
                        ivWeatherIcon.setImageResource(weatherIconRes);
                    }
                    if (illustrationBitmap != null) {
                        ivCityIllustration.setImageBitmap(illustrationBitmap);
                    }

                    showLoading(false);
                    tvError.setVisibility(View.GONE);
                    cardWeather.setVisibility(View.VISIBLE);
                }
            });

        } catch (final JSONException e) {
            showErrorOnUI("Error parsing weather data: " + e.getMessage());
        }
    }

    private String getWeatherConditionText(int code) {
        switch (code) {
            case 0: return "Clear Sky";
            case 1: case 2: case 3: return "Partly Cloudy";
            case 45: case 48: return "Foggy";
            case 51: case 53: case 55: case 56: case 57: return "Drizzle";
            case 61: case 63: case 65: case 66: case 67: return "Rainy";
            case 71: case 73: case 75: case 77: return "Snowy";
            case 95: case 96: case 99: return "Thunderstorm";
            default: return "Fair";
        }
    }

    private String getWeatherIconCode(int code) {
        switch (code) {
            case 0: return "01d"; // clear
            case 1: case 2: return "02d"; // partly cloudy
            case 3: return "04d"; // overcast
            case 45: case 48: return "50d"; // fog
            case 51: case 53: case 55: case 56: case 57: return "09d"; // drizzle
            case 61: case 63: case 65: case 66: case 67: return "10d"; // rain
            case 71: case 73: case 75: case 77: return "13d"; // snow
            case 95: case 96: case 99: return "11d"; // thunderstorm
            default: return "01d";
        }
    }

    private int getWeatherIconRes(int code) {
        switch (code) {
            case 0: return R.drawable.ic_weather_sun;
            case 1: case 2: case 3: return R.drawable.ic_cloud;
            case 51: case 53: case 55: case 56: case 57: return R.drawable.ic_rain;
            case 61: case 63: case 65: case 66: case 67: return R.drawable.ic_rain;
            case 71: case 73: case 75: case 77: return R.drawable.ic_snow;
            case 95: case 96: case 99: return R.drawable.ic_storm;
            default: return R.drawable.ic_weather_sun;
        }
    }

    private void showErrorOnUI(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showError(message);
                showLoading(false);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void showLoading(final boolean isLoading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isLoading) {
                    progressBar.setVisibility(View.VISIBLE);
                    cardWeather.setVisibility(View.GONE);
                    tvError.setVisibility(View.GONE);
                    btnSearch.setEnabled(false);
                    btnRefresh.setEnabled(false);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSearch.setEnabled(true);
                    btnRefresh.setEnabled(true);
                }
            }
        });
    }

    private void showError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvError.setText(message);
                tvError.setVisibility(View.VISIBLE);
                cardWeather.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
