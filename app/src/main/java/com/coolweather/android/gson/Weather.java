package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    @SerializedName("status")
    public String status;
    public Basic basic;
   // public AQI aqi;
    public Now now;
    public Update update;
//    public lifestyle suggestion;
//
//    @SerializedName("daily_forecast")
//    public List<Forecast> forecastList;
//
//    @SerializedName("lifestyle")
//    public List<lifestyle> lifestyleList;
}
