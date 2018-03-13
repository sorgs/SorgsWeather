package com.sorgs.sorgsweather.http;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 与服务器交互的类
 * Created by Sorgs
 * on 2017/5/9.
 */

public class OkHttp {

    /**
     * 发送http请求
     *
     * @param address 传入请求的url
     */
    public static Response sendOkHttpRequestGet(String address) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(address)
                .get();
        Request request = builder.build();
        Call call = new OkHttpClient().newCall(request);
        return call.execute();
    }
}
