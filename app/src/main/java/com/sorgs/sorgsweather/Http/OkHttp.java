package com.sorgs.sorgsweather.Http;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 与服务器交互的类
 * Created by Sorgs
 * on 2017/5/9.
 */

public class OkHttp {
    /**
     * 发送http请求
     *
     * @param address  传入请求的url
     * @param callback 回调处理服务器响应
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        //回调响应
        client.newCall(request).enqueue(callback);
    }
}
