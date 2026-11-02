<<<<<<< HEAD
# 🌤️ WeatherNow — Real-Time Android Weather Application
### Practical Experiment: *“Access Web Services Using HTTP Requests in Android”*

**WeatherNow** is a modern, production-grade Android application developed in **Java** and **XML layouts** that demonstrates accessing live web services through HTTP GET requests using `HttpURLConnection` and `ExecutorService`.

---

## 📂 Project Folder Structure
```text
Exp12/
├── app/
│   ├── build.gradle.kts                   # Module Gradle Dependencies (Material Components & KSP)
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml        # Manifest (INTERNET & ACCESS_NETWORK_STATE permissions)
│           ├── java/com/example/exp_12/
│           │   └── MainActivity.java      # Main Screen (HttpURLConnection + ExecutorService + JSON Parsing)
│           └── res/
│               ├── drawable/              # Gradients, Weather Sun, Cloud, Rain, Storm, Wind & Search Icons
│               ├── layout/
│               │   └── activity_main.xml  # Modern Material Design Weather UI Layout
│               └── values/                # Colors, Strings & Material3 Themes
├── 1.png                                  # Output Screenshot 1 (Main Weather Screen)
├── 2.png                                  # Output Screenshot 2 (Weather Details & Popular Cities)
└── README.md                              # Project Documentation
```

=======
# WeatherNow — Real-Time Android Weather Application
### Practical Experiment: *“Access Web Services Using HTTP Requests in Android”*

**WeatherNow** is a modern, production-grade Android application developed in **Java** and **XML layouts** that demonstrates accessing live web services through HTTP GET requests using `HttpURLConnection`.

---

## 🏗️ System Design & Architecture

The application follows a clean separation of concerns and robust multithreading design to ensure the UI remains responsive during network operations.

### Architecture Diagram
```mermaid
graph TD
    UI[User Interface / MainActivity] -->|User Input: City Name| ES[ExecutorService / Background Thread]
    ES -->|HTTP GET: Geocoding API| Geo[Open-Meteo Geocoding API]
    Geo -->|JSON Response: Lat/Lon| ES
    ES -->|HTTP GET: Forecast API| Weather[Open-Meteo Weather API]
    Weather -->|JSON Response: Weather Data| ES
    ES -->|HTTP GET: Public Icons & Images| Img[OpenWeatherMap & Picsum APIs]
    Img -->|Bitmaps| ES
    ES -->|runOnUiThread| UI
    UI -->|Render Weather Card & Stats| Screen[User Display]
```

### Key Technical Components:
1. **Networking Layer (`HttpURLConnection`)**:
   - Sends HTTP GET requests to public REST APIs without requiring API keys (Open-Meteo & OpenWeatherMap public image endpoints).
   - Configured with strict connection (10s) and read (10s) timeouts.
2. **Asynchronous Background Execution (`ExecutorService`)**:
   - Network requests, JSON parsing, and bitmap image decoding are executed on a dedicated background thread using `Executors.newSingleThreadExecutor()`.
3. **Main Thread UI Synchronization (`runOnUiThread`)**:
   - Safely updates UI views (`TextView`, `ImageView`, `ProgressBar`) once network data and bitmaps are successfully processed.
4. **Robust Error Handling**:
   - Catches network connectivity drops, timeouts, malformed JSON responses, and invalid city names with user-friendly error messages.

>>>>>>> ac7e570a822847989f74063f69182dc304897f10
---

## 📱 Output Screenshots

<<<<<<< HEAD
### 1. Main Weather Screen (`MainActivity.java`)
![WeatherNow Main Screen](./1.png)

### 2. Weather Details & Popular Cities (`activity_main.xml`)
=======
Here is the application running on an Android device:

### 1. Main Weather Screen (Default / Searched City)
![WeatherNow Main Screen](./1.png)

### 2. Weather Details & Quick Chips
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
![WeatherNow Details](./2.png)

---

<<<<<<< HEAD
## 💻 Complete Source Code
=======
## 💻 Core Code Implementation
>>>>>>> ac7e570a822847989f74063f69182dc304897f10

### 1. `MainActivity.java`
```java
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

<<<<<<< HEAD
        // Popular City Quick Chips
=======
        // Popular City Chips
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
        findViewById(R.id.chipBengaluru).setOnClickListener(v -> loadCityWeather("Bengaluru"));
        findViewById(R.id.chipLondon).setOnClickListener(v -> loadCityWeather("London"));
        findViewById(R.id.chipNewYork).setOnClickListener(v -> loadCityWeather("New York"));
        findViewById(R.id.chipTokyo).setOnClickListener(v -> loadCityWeather("Tokyo"));
<<<<<<< HEAD
        findViewById(R.id.chipParis).setOnClickListener(v -> loadCityWeather("Paris"));
        findViewById(R.id.chipMumbai).setOnClickListener(v -> loadCityWeather("Mumbai"));
=======
>>>>>>> ac7e570a822847989f74063f69182dc304897f10

        btnSearch.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            if (TextUtils.isEmpty(city)) {
                etCity.setError("Please enter a city name");
                return;
            }
            hideKeyboard();
            lastSearchedCity = city;
            fetchWeatherData(city);
        });

        btnRefresh.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(lastSearchedCity)) {
                fetchWeatherData(lastSearchedCity);
            }
        });

<<<<<<< HEAD
        // Load default city on app startup
=======
        // Load default city on startup
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
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
            showError("Network error. Please check your internet connection.");
            return;
        }

        showLoading(true);

        executorService.execute(() -> {
            try {
<<<<<<< HEAD
                // Step 1: Geocoding API to get Latitude and Longitude (No API Key required)
                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + 
                        java.net.URLEncoder.encode(cityName, "UTF-8") + "&count=1";
                
=======
                // Step 1: Geocoding
                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + 
                        java.net.URLEncoder.encode(cityName, "UTF-8") + "&count=1";
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
                String geoJsonResponse = makeHttpGETRequest(geoUrlStr);
                if (geoJsonResponse == null) {
                    showErrorOnUI("Network error while connecting to server.");
                    return;
                }

                JSONObject geoObj = new JSONObject(geoJsonResponse);
                if (!geoObj.has("results")) {
                    showErrorOnUI("City not found. Please check spelling.");
                    return;
                }

                JSONObject cityObj = geoObj.getJSONArray("results").getJSONObject(0);
                double lat = cityObj.getDouble("latitude");
                double lon = cityObj.getDouble("longitude");
                String name = cityObj.getString("name");
                String country = cityObj.has("country") ? cityObj.getString("country") : "";

<<<<<<< HEAD
                // Step 2: Open-Meteo Weather Forecast API
=======
                // Step 2: Weather Forecast API
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
                String weatherUrlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + 
                        "&longitude=" + lon + 
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" + 
                        "&daily=temperature_2m_max,temperature_2m_min&timezone=auto";
<<<<<<< HEAD

=======
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
                String weatherJsonResponse = makeHttpGETRequest(weatherUrlStr);
                if (weatherJsonResponse == null) {
                    showErrorOnUI("Failed to fetch weather forecast.");
                    return;
                }

<<<<<<< HEAD
                // Step 3: Download weather icon and city illustration bitmap from public APIs
=======
                // Step 3: Load public API images
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
                int weatherCode = new JSONObject(weatherJsonResponse).getJSONObject("current").getInt("weather_code");
                String iconCode = getWeatherIconCode(weatherCode);
                String iconUrlStr = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
                String illustrationUrlStr = "https://picsum.photos/seed/" + java.net.URLEncoder.encode(cityName, "UTF-8") + "/800/300";

                Bitmap weatherBitmap = downloadBitmapFromUrl(iconUrlStr);
                Bitmap illustrationBitmap = downloadBitmapFromUrl(illustrationUrlStr);

                parseAndDisplayWeather(weatherJsonResponse, name, country, weatherBitmap, illustrationBitmap);

            } catch (Exception e) {
                showErrorOnUI("Error: " + e.getMessage());
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

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
            // Ignored
        } finally {
            if (connection != null) connection.disconnect();
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
            if (connection != null) connection.disconnect();
        }
    }

    private void parseAndDisplayWeather(String jsonResponse, String cityName, String country, Bitmap weatherBitmap, Bitmap illustrationBitmap) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject currentObj = jsonObject.getJSONObject("current");
            JSONObject dailyObj = jsonObject.getJSONObject("daily");

            double temp = currentObj.getDouble("temperature_2m");
            double feelsLike = currentObj.getDouble("apparent_temperature");
            int humidity = currentObj.getInt("relative_humidity_2m");
            double windSpeed = currentObj.getDouble("wind_speed_10m");
            int weatherCode = currentObj.getInt("weather_code");

            double tempMax = dailyObj.getJSONArray("temperature_2m_max").getDouble(0);
            double tempMin = dailyObj.getJSONArray("temperature_2m_min").getDouble(0);

            String displayCity = TextUtils.isEmpty(country) ? cityName : cityName + ", " + country;
            String displayTemp = Math.round(temp) + "°C";
            String displayCondition = getWeatherConditionText(weatherCode);
            String displayFeelsLike = "Feels like: " + Math.round(feelsLike) + "°C";
            String displayHumidity = humidity + "%";
            String displayWind = Math.round(windSpeed) + " km/h";
            String displayMinTemp = Math.round(tempMin) + "°C";
            String displayMaxTemp = Math.round(tempMax) + "°C";
            int weatherIconRes = getWeatherIconRes(weatherCode);

            runOnUiThread(() -> {
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
            });

        } catch (JSONException e) {
            showErrorOnUI("Error parsing weather JSON: " + e.getMessage());
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
            case 0: return "01d";
            case 1: case 2: return "02d";
            case 3: return "04d";
            case 45: case 48: return "50d";
            case 51: case 53: case 55: case 56: case 57: return "09d";
            case 61: case 63: case 65: case 66: case 67: return "10d";
            case 71: case 73: case 75: case 77: return "13d";
            case 95: case 96: case 99: return "11d";
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

    private void showErrorOnUI(String message) {
        runOnUiThread(() -> {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
            cardWeather.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
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

    private void showLoading(boolean isLoading) {
        runOnUiThread(() -> {
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
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
            cardWeather.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
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
```

<<<<<<< HEAD
### 2. `activity_main.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_gradient"
    android:fillViewport="true"
    tools:context=".MainActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingHorizontal="20dp"
        android:paddingTop="40dp"
        android:paddingBottom="20dp">

        <!-- Top Header Banner -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:layout_marginBottom="16dp"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <ImageView
                android:layout_width="52dp"
                android:layout_height="52dp"
                android:contentDescription="@string/app_name"
                android:src="@drawable/ic_weather_sun" />

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginStart="12dp"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/app_name"
                    android:textColor="@color/white"
                    android:textSize="26sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Know the Weather, Anywhere!"
                    android:textColor="#E0E0E0"
                    android:textSize="13sp" />

            </LinearLayout>

        </LinearLayout>

        <!-- Search Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <EditText
                android:id="@+id/etCity"
                android:layout_width="0dp"
                android:layout_height="54dp"
                android:layout_marginEnd="10dp"
                android:layout_weight="1"
                android:autofillHints="name"
                android:background="@drawable/rounded_card_bg"
                android:drawableStart="@drawable/ic_search"
                android:drawablePadding="12dp"
                android:hint="@string/city_hint"
                android:inputType="textPersonName"
                android:maxLines="1"
                android:paddingHorizontal="16dp"
                android:textColor="@color/text_primary"
                android:textColorHint="@color/text_secondary"
                android:textSize="16sp" />

            <Button
                android:id="@+id/btnSearch"
                android:layout_width="wrap_content"
                android:layout_height="54dp"
                android:backgroundTint="#5C35D6"
                android:text="@string/search"
                android:textAllCaps="false"
                android:textColor="@color/white"
                android:textSize="16sp"
                app:cornerRadius="27dp" />

        </LinearLayout>

        <!-- Popular Cities Quick Chips -->
        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="20dp"
            android:scrollbars="none">

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.chip.Chip
                    android:id="@+id/chipBengaluru"
                    style="@style/Widget.Material3.Chip.Assist"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp"
                    android:text="Bengaluru"
                    android:textColor="@color/text_primary"
                    app:chipBackgroundColor="#FFFFFF"
                    app:chipStrokeColor="#E0E0E0"
                    app:chipStrokeWidth="1dp" />

                <com.google.android.material.chip.Chip
                    android:id="@+id/chipLondon"
                    style="@style/Widget.Material3.Chip.Assist"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp"
                    android:text="London"
                    android:textColor="@color/text_primary"
                    app:chipBackgroundColor="#FFFFFF"
                    app:chipStrokeColor="#E0E0E0"
                    app:chipStrokeWidth="1dp" />

                <com.google.android.material.chip.Chip
                    android:id="@+id/chipNewYork"
                    style="@style/Widget.Material3.Chip.Assist"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp"
                    android:text="New York"
                    android:textColor="@color/text_primary"
                    app:chipBackgroundColor="#FFFFFF"
                    app:chipStrokeColor="#E0E0E0"
                    app:chipStrokeWidth="1dp" />

                <com.google.android.material.chip.Chip
                    android:id="@+id/chipTokyo"
                    style="@style/Widget.Material3.Chip.Assist"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Tokyo"
                    android:textColor="@color/text_primary"
                    app:chipBackgroundColor="#FFFFFF"
                    app:chipStrokeColor="#E0E0E0"
                    app:chipStrokeWidth="1dp" />

            </LinearLayout>
        </HorizontalScrollView>

        <!-- Weather Card Container -->
        <LinearLayout
            android:id="@+id/cardWeather"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:visibility="visible">

            <!-- Main Weather Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardBackgroundColor="#CCFFFFFF"
                app:cardCornerRadius="24dp"
                app:cardElevation="6dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:gravity="center_horizontal"
                    android:orientation="vertical"
                    android:padding="24dp">

                    <!-- Location row -->
                    <LinearLayout
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:gravity="center_vertical"
                        android:orientation="horizontal">

                        <ImageView
                            android:layout_width="20dp"
                            android:layout_height="20dp"
                            android:contentDescription="Location"
                            android:src="@drawable/ic_location_pin" />

                        <TextView
                            android:id="@+id/tvCityName"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="6dp"
                            android:textColor="@color/text_primary"
                            android:textSize="20sp"
                            android:textStyle="bold"
                            tools:text="Bengaluru, India" />

                    </LinearLayout>

                    <!-- Large Weather Icon & Temp Row -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="16dp"
                        android:gravity="center"
                        android:orientation="horizontal">

                        <ImageView
                            android:id="@+id/ivWeatherIcon"
                            android:layout_width="90dp"
                            android:layout_height="90dp"
                            android:contentDescription="Weather Icon"
                            android:src="@drawable/ic_weather_sun" />

                        <TextView
                            android:id="@+id/tvTemperature"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="16dp"
                            android:textColor="@color/text_primary"
                            android:textSize="56sp"
                            android:textStyle="bold"
                            tools:text="26°C" />

                    </LinearLayout>

                    <TextView
                        android:id="@+id/tvCondition"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="8dp"
                        android:textColor="@color/text_primary"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        tools:text="Partly Cloudy" />

                    <View
                        android:layout_width="160dp"
                        android:layout_height="2dp"
                        android:layout_marginVertical="10dp"
                        android:background="#BDBDBD" />

                    <TextView
                        android:id="@+id/tvFeelsLike"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textColor="@color/text_secondary"
                        android:textSize="15sp"
                        tools:text="Feels like: 27°C" />

                </LinearLayout>

            </androidx.cardview.widget.CardView>

            <!-- Cityscape Illustration loaded from Public API -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="140dp"
                android:layout_marginBottom="16dp"
                app:cardBackgroundColor="#CCFFFFFF"
                app:cardCornerRadius="20dp"
                app:cardElevation="4dp">

                <ImageView
                    android:id="@+id/ivCityIllustration"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:contentDescription="City Illustration"
                    android:scaleType="centerCrop"
                    android:src="@drawable/ic_weather_sun" />

            </androidx.cardview.widget.CardView>

        </LinearLayout>

    </LinearLayout>

</ScrollView>
```
=======
---

### 2. `AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.WeatherNow">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

## 🚀 How to Build and Run
1. Open the project in Android Studio.
2. Ensure you have internet connection enabled.
3. Click **Run 'app'** (`Shift + F10`) to build and deploy to your emulator or device.
>>>>>>> ac7e570a822847989f74063f69182dc304897f10
