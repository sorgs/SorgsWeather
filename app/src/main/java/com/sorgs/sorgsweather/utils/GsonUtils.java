package com.sorgs.sorgsweather.utils;

import com.google.gson.Gson;

/**
 * description: Gson单例.
 *
 * @author Sorgs.
 * @date 2018/3/7.
 */

public class GsonUtils {
    private static class GsonHolder {
        private static final Gson INSTANCE = new Gson();
    }

    /**
     * 获取Gson实例，由于Gson是线程安全的，这里共同使用同一个Gson实例
     */
    public static Gson getGsonInstance() {
        return GsonHolder.INSTANCE;
    }
}
