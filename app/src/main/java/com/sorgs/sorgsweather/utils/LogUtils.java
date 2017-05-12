package com.sorgs.sorgsweather.utils;

import android.util.Log;

/**
 * 自定义log类
 * <p>
 * Created by Sorgs
 * on 2017/5/12.
 */

public class LogUtils {

    public static final int INFO = 3;
    /**
     * 可指定是否打印
     */
    public static final int level = 0;

    public static void i(String tag, String mag) {
        if (level <= INFO) {
            Log.i(tag, mag);
        }

    }
}
