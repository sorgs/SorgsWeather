package com.sorgs.sorgsweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;

import com.sorgs.sorgsweather.Http.OkHttp;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.Sputils;
import com.sorgs.sorgsweather.utils.Utility;
import com.sorgs.sorgsweather.utils.getCache;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 后天自动更新
 * <p>
 * Created by Sorgs
 * on 2017/5/12.
 */

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updatePic();
        //设置更新时间
        int anHour = 3 * 60 * 60 * 1000;//设置3小时更新一次
        //开始延迟服务
        AlarmManager systemService = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent service = PendingIntent.getService(this, 0, i, 0);
        systemService.cancel(service);
        systemService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, service);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新背景图
     */
    private void updatePic() {
        OkHttp.sendOkHttpRequest(Constant.PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String pic = response.body().string();
                //保存缓存的图片
                if (!TextUtils.isEmpty(pic)) {
                    Sputils.putString(getApplication(), Constant.PIC, pic);
                }
            }
        });
    }

    /**
     * 跟新天气信息
     */
    private void updateWeather() {

        String Weather = getCache.getCityID(Sputils.getString(getApplicationContext(), Constant.WEATHER, null));
        if (!TextUtils.isEmpty(Weather)) {
            //存在缓存，就直接去解析
            WeatherJson weatherJson = Utility.handleWeatherResponse(Sputils.getString(getApplicationContext(), Constant.WEATHER, null));
            assert weatherJson != null;
            for (WeatherJson.HeWeather5Bean heWeatherBean :
                    weatherJson.getHeWeather5()) {
                if ("ok".equals(heWeatherBean.getStatus())) {
                    String weatherId = heWeatherBean.getBasic().getCity();
                    OkHttp.sendOkHttpRequest(Constant.WEATHER_URL + weatherId + Constant.WEATHER_KEY, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String string = response.body().string();
                            //是否能获取城市id 缓存Json数据
                            if (!TextUtils.isEmpty(getCache.getCityID(string))) {
                                Sputils.putString(getApplicationContext(), Constant.WEATHER, string);
                            }

                        }
                    });
                }
            }

        }
    }
}

