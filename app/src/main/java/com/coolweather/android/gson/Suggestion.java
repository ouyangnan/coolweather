package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Suggestion {
    @SerializedName("lifestyle")
    public List<Life> lifestyleList;
}
