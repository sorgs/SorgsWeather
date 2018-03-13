package com.sorgs.sorgsweather.service;

import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.http.OkHttp;
import com.sorgs.sorgsweather.model.WeatherViewModel;
import com.sorgs.sorgsweather.ui.activity.MyApplication;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.GsonUtils;
import com.sorgs.sorgsweather.utils.SharedPreferencesUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

/**
 * 后天自动更新
 * <p>
 * Created by Sorgs
 * on 2017/5/12.
 */

public class AutoUpdateService extends Service {

    private Disposable mDisposable;
    private UpdateListener mUpdateListener;
    private WeatherViewModel mWeatherViewModel;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new UpDataBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //后台更新数据

        autoUpdateWeather();
    }

    /**
     * 每隔三个小时获取最新数据
     */
    private void autoUpdateWeather() {
        mDisposable = Flowable
                .interval(3, TimeUnit.HOURS)
                .subscribeOn(Schedulers.io())
                .map(aLong -> SharedPreferencesUtils.getString(MyApplication.getInstance().mContext, Constant.WEATHER, null))
                .filter(s -> !TextUtils.isEmpty(s))
                .map(s -> {
                    String cityId = null;
                    WeatherJson weatherJson = GsonUtils.getGsonInstance().fromJson(s, WeatherJson.class);
                    for (WeatherJson.HeWeather5Bean weather5Bean : weatherJson.getHeWeather5()) {
                        cityId = weather5Bean.getBasic().getId();
                    }
                    return cityId;
                })
                .filter(s -> !TextUtils.isEmpty(s))
                .map(s -> {
                    String weatherJson = null;
                    Response response = OkHttp.sendOkHttpRequestGet(Constant.WEATHER_URL + s + Constant.WEATHER_KEY);
                    if (response.isSuccessful() || response.body() != null) {
                        weatherJson = response.body().string();
                        SharedPreferencesUtils.putString(MyApplication.getInstance().mContext, Constant.WEATHER_KEY, weatherJson);
                    }
                    return weatherJson;
                }).filter(s -> !TextUtils.isEmpty(s))
                .map(s -> GsonUtils.getGsonInstance().fromJson(s, WeatherJson.class))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weatherJson -> mUpdateListener.UpDate(weatherJson));
    }


    public interface UpdateListener {
        void UpDate(WeatherJson weatherJson);
    }

    /**
     * 注册回调接口的方法，供外部调用
     */
    public void setUpDataListener(UpdateListener updateListener) {
        mUpdateListener = updateListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        stopSelf();
    }

    public class UpDataBinder extends Binder {
        public AutoUpdateService getService() {
            return AutoUpdateService.this;
        }
    }
}

