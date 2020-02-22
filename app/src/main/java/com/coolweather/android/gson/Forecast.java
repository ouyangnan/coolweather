package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;

    @SerializedName("tmp_max")
    public String max;

    @SerializedName("tmp_min")
    public String min;

    @SerializedName("cond_txt_d")
    //public More more;
    public String day;

    @SerializedName("cond_txt_n")
    public String night;

//    public class Temperature{
//        public String max;
//        public String min;
//    }

//    public class More{
//        @SerializedName("txt_d")
//        public String info;
//    }
}
