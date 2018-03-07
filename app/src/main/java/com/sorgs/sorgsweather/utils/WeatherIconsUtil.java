package com.sorgs.sorgsweather.utils;

import com.sorgs.sorgsweather.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * description: 操作天气图标工具类.
 *
 * @author Sorgs.
 * @date 2018/3/1.
 */

public class WeatherIconsUtil {
    /**
     * 根据天气获取对应的图标，并且缩放到指定大小
     *
     * @param weather  天气
     * @param requestW 图片的宽
     * @param requestH 图片的高
     * @return bitmap
     */
    public static Bitmap getWeatherIcon(Context context, String weather, float requestW, float requestH) {

        int resId = getIconResId(weather);

        BitmapFactory.Options options = new BitmapFactory.Options();
        //开启允许查询图片的信息
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inSampleSize = 1;
        if (outWidth > requestW || outHeight > requestH) {
            int ratioW = Math.round(outWidth / requestW);
            int ratioH = Math.round(outHeight / requestH);
            options.inSampleSize = Math.max(ratioW, ratioH);
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    private static int getIconResId(String weather) {
        int resId = R.mipmap.w_sun;
        if (weather.contains("云")) {
            resId = R.mipmap.w_cloud;
        } else if (weather.contains("晴")) {
            resId = R.mipmap.w_sun;
        } else if (weather.contains("雪")) {
            resId = R.mipmap.w_snow;
        } else if (weather.contains("雨")) {
            resId = R.mipmap.w_rain;
        } else if (weather.contains("雷")) {
            resId = R.mipmap.w_thunder;
        }
        return resId;
    }
}
