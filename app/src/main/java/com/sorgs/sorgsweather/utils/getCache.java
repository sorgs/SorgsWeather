package com.sorgs.sorgsweather.utils;

import android.text.TextUtils;

import com.sorgs.sorgsweather.domian.WeatherJson;

/**
 * 获取缓存的东西
 * Created by Sorgs
 * on 2017/5/15.
 */

public class GetCache {
    /**
     * 从缓存的中获取城市id
     *
     * @param WeatherCache 传入需要判断的json
     * @return 返回城市id
     */
    public static String getCityID(String WeatherCache) {
        if (!TextUtils.isEmpty(WeatherCache)) {
            WeatherJson weatherJson = HandleUtility.handleWeatherResponse(WeatherCache);
            for (WeatherJson.HeWeather5Bean heWeatherBean :
                    weatherJson.getHeWeather5()) {
                if ("ok".equals(heWeatherBean.getStatus())) {
                    return heWeatherBean.getBasic().getCity();
                }
            }
        }
        return null;
    }

}
