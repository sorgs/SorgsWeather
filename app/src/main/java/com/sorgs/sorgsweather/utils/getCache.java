package com.sorgs.sorgsweather.utils;

import com.sorgs.sorgsweather.domian.WeatherJson;

import android.text.TextUtils;

/**
 * description: xxx.
 *
 * @author Sorgs.
 * @date 2018/3/5.
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
