package com.sorgs.sorgsweather.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户缓存
 * <p>
 * Created by Sorgs
 * on 2017/5/11.
 */

public class Sputils {

    private static SharedPreferences sharedPreferences;

    /**
     * 写入String变量到sharedPreferences中
     *
     * @param context 上下文环境
     * @param key     存储节点名称
     * @param value   储存节点的值boolean
     */
    public static void putString(Context context, String key, String value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * 读取String表示从getSharedPreferences中
     *
     * @param context  上下文环境
     * @param key      储存节点名称
     * @param defValue 没有此节点的默认值
     * @return 默认值或者节点读取到的结果
     */
    public static String getString(Context context, String key, String defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(key, defValue);
    }
}
