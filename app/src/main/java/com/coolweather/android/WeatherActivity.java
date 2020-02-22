package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Life;
import com.coolweather.android.gson.Suggestion;
import com.coolweather.android.gson.ThreeDayForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.prefs.Preferences;

import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
    }

    private void initView(){
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        String forecastString = prefs.getString("forecast",null);
        String suggestString = prefs.getString("suggest",null);
//        Log.d("weatherString= ",  weatherString);
        final String countyName;
        if (weatherString != null && forecastString != null && suggestString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            ThreeDayForecast forecast = Utility.handlerForecastResponse(forecastString);
            Suggestion suggest = Utility.handlerSuggestionResponse(suggestString);
            countyName = weather.basic.cityName;
            showWeatherInfo(weather);
            showThreeDayForecast(forecast);
            showSuggestion(suggest);
        }else {
            countyName = getIntent().getStringExtra("county_name");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyName,"now");
            requestWeather(countyName,"forecast");
            requestWeather(countyName,"lifestyle");
        }



        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(countyName,"now");
                requestWeather(countyName,"forecast");
                requestWeather(countyName,"lifestyle");
            }
        });

        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null)
            Glide.with(this).load(bingPic).into(bingPicImg);
        else
            loadBingPic();


        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    public void requestWeather(final String countyName,final String re){

       // String[] request = {"now","forecast","lifestyle"};
        String weatherUrl = "https://free-api.heweather.net/s6/weather/"+ re +"?location="  +
                countyName + "&key=661611845e1545bb8994f612574a21aa";
        switch(re){
            case "now" :
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                        Toast.LENGTH_SHORT).show();
                              //  Log.d("onFailure","获取天气信息失败");
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response)
                            throws IOException {
                        final String responseText = response.body().string();
                      //  Log.d("onResponse",responseText);
                        final Weather weather = Utility.handleWeatherResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null && "ok".equals(weather.status)){
                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences(WeatherActivity.this).
                                            edit();
                                    editor.putString("weather",responseText);
                                    editor.apply();
                                    showWeatherInfo(weather);

                                }else {
                                    Toast.makeText(WeatherActivity.this,
                                            "获取天气信息失败", Toast.LENGTH_SHORT).show();
                                    //Log.d("onResponse","获取天气信息失败");
                                }
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
            break;
            case "forecast" :
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("onFailure","获取天气信息失败");
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response)
                            throws IOException {
                        final String responseText = response.body().string();
                          //Log.d("onResponse",responseText);
                        final ThreeDayForecast forecast= Utility.handlerForecastResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ( forecast != null ){
                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences(WeatherActivity.this).
                                            edit();
                                    editor.putString("forecast",responseText);
                                    editor.apply();
                                    showThreeDayForecast(forecast);

                                }else {
                                    Toast.makeText(WeatherActivity.this,
                                            "获取天气信息失败", Toast.LENGTH_SHORT).show();
                                    Log.d("onResponse","获取天气信息失败");
                                }
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
            break;
            case "lifestyle" :
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("onFailure","获取天气信息失败");
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response)
                            throws IOException {
                        final String responseText = response.body().string();
                       //   Log.d("onResponse",responseText);
                        final Suggestion suggest = Utility.handlerSuggestionResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (suggest != null ){
                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences(WeatherActivity.this).
                                            edit();
                                    editor.putString("suggest",responseText);
                                    editor.apply();
                                    showSuggestion(suggest);

                                }else {
                                    Toast.makeText(WeatherActivity.this,
                                            "获取天气信息失败", Toast.LENGTH_SHORT).show();
                                   // Log.d("onResponse","获取天气信息失败");
                                }
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
                break;
        }


        loadBingPic();
    }

    public void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.cond;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        weatherLayout.setVisibility(View.VISIBLE);

    }

    public void showThreeDayForecast(ThreeDayForecast tdf){
        forecastLayout.removeAllViews();
        for (Forecast forecast : tdf.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            if (!forecast.day.equals(forecast.night)) {
                String cast = forecast.day + "转" + forecast.night;
                infoText.setText(cast);
            } else
                infoText.setText(forecast.day);
            maxText.setText(forecast.max);
            minText.setText(forecast.min);
            forecastLayout.addView(view);
        }
//
//        if (weather.aqi != null){
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
//
//        String comfort = "舒适度：" + weather.suggestion.comfort.info;
//        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
//        String sport = "运动建议：" + weather.suggestion.sport.info;
//        comfortText.setText(comfort);
//        carWashText.setText(carWash);
//        sportText.setText(sport);
    }

    public void showSuggestion(Suggestion suggest){
        for (Life lifestyle : suggest.lifestyleList) {
            switch (lifestyle.type){
                case "comf" :
                    String comfort = "舒适度：" + lifestyle.brf;
                    comfortText.setText(comfort);
                    break;
                case "cw" :
                    String carWash = "洗车指数：" + lifestyle.txt;
                    carWashText.setText(carWash);
                    break;
                case "sport" :
                    String sport = "运动建议：" + lifestyle.txt;
                    sportText.setText(sport);
                    break;

            }






        }
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        (WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
