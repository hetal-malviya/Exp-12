package com.example.exp_12

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var etCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnRefresh: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvCityName: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvMinTemp: TextView
    private lateinit var tvMaxTemp: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var ivCityIllustration: ImageView
    private lateinit var cardWeather: View

    private lateinit var executorService: ExecutorService
    private var lastSearchedCity = "Bengaluru"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCity = findViewById(R.id.etCity)
        btnSearch = findViewById(R.id.btnSearch)
        btnRefresh = findViewById(R.id.btnRefresh)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvCityName = findViewById(R.id.tvCityName)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCondition = findViewById(R.id.tvCondition)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvMinTemp = findViewById(R.id.tvMinTemp)
        tvMaxTemp = findViewById(R.id.tvMaxTemp)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        ivCityIllustration = findViewById(R.id.ivCityIllustration)
        cardWeather = findViewById(R.id.cardWeather)

        executorService = Executors.newSingleThreadExecutor()

        findViewById<Chip>(R.id.chipBengaluru).setOnClickListener { loadCityWeather("Bengaluru") }
        findViewById<Chip>(R.id.chipLondon).setOnClickListener { loadCityWeather("London") }
        findViewById<Chip>(R.id.chipNewYork).setOnClickListener { loadCityWeather("New York") }
        findViewById<Chip>(R.id.chipTokyo).setOnClickListener { loadCityWeather("Tokyo") }

        btnSearch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (TextUtils.isEmpty(city)) {
                etCity.error = "Please enter a city name"
                return@setOnClickListener
            }
            hideKeyboard()
            lastSearchedCity = city
            fetchWeatherData(city)
        }

        btnRefresh.setOnClickListener {
            if (!TextUtils.isEmpty(lastSearchedCity)) {
                fetchWeatherData(lastSearchedCity)
            }
        }

        etCity.setText("Bengaluru")
        fetchWeatherData("Bengaluru")
    }

    private fun loadCityWeather(city: String) {
        etCity.setText(city)
        hideKeyboard()
        lastSearchedCity = city
        fetchWeatherData(city)
    }

    private fun fetchWeatherData(cityName: String) {
        if (!isNetworkAvailable()) {
            showError("Network error. Please check your internet connection.")
            return
        }

        showLoading(true)

        executorService.execute {
            try {
                // Step 1: Geocoding API to get Latitude and Longitude (No API Key required)
                val geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + 
                        URLEncoder.encode(cityName, "UTF-8") + "&count=1"
                
                val geoJsonResponse = makeHttpGETRequest(geoUrlStr)
                if (geoJsonResponse == null) {
                    showErrorOnUI("Network error while connecting to server.")
                    return@execute
                }

                val geoObj = JSONObject(geoJsonResponse)
                if (!geoObj.has("results")) {
                    showErrorOnUI("City not found. Please check spelling.")
                    return@execute
                }

                val cityObj = geoObj.getJSONArray("results").getJSONObject(0)
                val lat = cityObj.getDouble("latitude")
                val lon = cityObj.getDouble("longitude")
                val name = cityObj.getString("name")
                val country = if (cityObj.has("country")) cityObj.getString("country") else ""

                // Step 2: Open-Meteo Weather Forecast API
                val weatherUrlStr = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" + 
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" + 
                        "&daily=temperature_2m_max,temperature_2m_min&timezone=auto"

                val weatherJsonResponse = makeHttpGETRequest(weatherUrlStr)
                if (weatherJsonResponse == null) {
                    showErrorOnUI("Failed to fetch weather forecast.")
                    return@execute
                }

                // Step 3: Download weather icon and city illustration bitmap from public APIs
                val weatherCode = JSONObject(weatherJsonResponse).getJSONObject("current").getInt("weather_code")
                val iconCode = getWeatherIconCode(weatherCode)
                val iconUrlStr = "https://openweathermap.org/img/wn/$iconCode@4x.png"
                val illustrationUrlStr = "https://picsum.photos/seed/" + URLEncoder.encode(cityName, "UTF-8") + "/800/300"

                val weatherBitmap = downloadBitmapFromUrl(iconUrlStr)
                val illustrationBitmap = downloadBitmapFromUrl(illustrationUrlStr)

                parseAndDisplayWeather(weatherJsonResponse, name, country, weatherBitmap, illustrationBitmap)

            } catch (e: Exception) {
                showErrorOnUI("Error: ${e.message}")
            }
        }
    }

    private fun makeHttpGETRequest(requestUrl: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(requestUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            }
        } catch (e: Exception) {
            // Ignored
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun downloadBitmapFromUrl(urlString: String): Bitmap? {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()
            inputStream = connection.inputStream
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            return null
        } finally {
            try { inputStream?.close() } catch (ignored: Exception) {}
            connection?.disconnect()
        }
    }

    private fun parseAndDisplayWeather(jsonResponse: String, cityName: String, country: String, weatherBitmap: Bitmap?, illustrationBitmap: Bitmap?) {
        try {
            val jsonObject = JSONObject(jsonResponse)
            val currentObj = jsonObject.getJSONObject("current")
            val dailyObj = jsonObject.getJSONObject("daily")

            val temp = currentObj.getDouble("temperature_2m")
            val feelsLike = currentObj.getDouble("apparent_temperature")
            val humidity = currentObj.getInt("relative_humidity_2m")
            val windSpeed = currentObj.getDouble("wind_speed_10m")
            val weatherCode = currentObj.getInt("weather_code")

            val tempMax = dailyObj.getJSONArray("temperature_2m_max").getDouble(0)
            val tempMin = dailyObj.getJSONArray("temperature_2m_min").getDouble(0)

            val displayCity = if (TextUtils.isEmpty(country)) cityName else "$cityName, $country"
            val displayTemp = "${Math.round(temp)}°C"
            val displayCondition = getWeatherConditionText(weatherCode)
            val displayFeelsLike = "Feels like: ${Math.round(feelsLike)}°C"
            val displayHumidity = "$humidity%"
            val displayWind = "${Math.round(windSpeed)} km/h"
            val displayMinTemp = "${Math.round(tempMin)}°C"
            val displayMaxTemp = "${Math.round(tempMax)}°C"
            val weatherIconRes = getWeatherIconRes(weatherCode)

            runOnUiThread {
                tvCityName.text = displayCity
                tvTemperature.text = displayTemp
                tvCondition.text = displayCondition
                tvFeelsLike.text = displayFeelsLike
                tvHumidity.text = displayHumidity
                tvWindSpeed.text = displayWind
                tvMinTemp.text = displayMinTemp
                tvMaxTemp.text = displayMaxTemp

                if (weatherBitmap != null) {
                    ivWeatherIcon.setImageBitmap(weatherBitmap)
                } else {
                    ivWeatherIcon.setImageResource(weatherIconRes)
                }
                if (illustrationBitmap != null) {
                    ivCityIllustration.setImageBitmap(illustrationBitmap)
                }

                showLoading(false)
                tvError.visibility = View.GONE
                cardWeather.visibility = View.VISIBLE
            }

        } catch (e: JSONException) {
            showErrorOnUI("Error parsing weather JSON: ${e.message}")
        }
    }

    private fun getWeatherConditionText(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67 -> "Rainy"
            71, 73, 75, 77 -> "Snowy"
            95, 96, 99 -> "Thunderstorm"
            else -> "Fair"
        }
    }

    private fun getWeatherIconCode(code: Int): String {
        return when (code) {
            0 -> "01d"
            1, 2 -> "02d"
            3 -> "04d"
            45, 48 -> "50d"
            51, 53, 55, 56, 57 -> "09d"
            61, 63, 65, 66, 67 -> "10d"
            71, 73, 75, 77 -> "13d"
            95, 96, 99 -> "11d"
            else -> "01d"
        }
    }

    private fun getWeatherIconRes(code: Int): Int {
        return when (code) {
            0 -> R.drawable.ic_weather_sun
            1, 2, 3 -> R.drawable.ic_cloud
            51, 53, 55, 56, 57 -> R.drawable.ic_rain
            61, 63, 65, 66, 67 -> R.drawable.ic_rain
            71, 73, 75, 77 -> R.drawable.ic_snow
            95, 96, 99 -> R.drawable.ic_storm
            else -> R.drawable.ic_weather_sun
        }
    }

    private fun showErrorOnUI(message: String) {
        runOnUiThread {
            tvError.text = message
            tvError.visibility = View.VISIBLE
            cardWeather.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = cm?.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                cardWeather.visibility = View.GONE
                tvError.visibility = View.GONE
                btnSearch.isEnabled = false
                btnRefresh.isEnabled = false
            } else {
                progressBar.visibility = View.GONE
                btnSearch.isEnabled = true
                btnRefresh.isEnabled = true
            }
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            tvError.text = message
            tvError.visibility = View.VISIBLE
            cardWeather.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }
}
