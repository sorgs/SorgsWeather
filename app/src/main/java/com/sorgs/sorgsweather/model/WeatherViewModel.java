package com.sorgs.sorgsweather.model;


import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.http.OkHttp;
import com.sorgs.sorgsweather.ui.activity.MyApplication;
import com.sorgs.sorgsweather.utils.GsonUtils;
import com.sorgs.sorgsweather.utils.SharedPreferencesUtils;

import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;


public class WeatherViewModel extends ViewModel {

    /**
     * 加载天气数据
     */
    public Observable<WeatherJson> getWeather(String city) {
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            Response response = OkHttp.sendOkHttpRequestGet(MyApplication.getInstance().mContext.getString(R.string.weather_url)
                    + city + MyApplication.getInstance().mContext.getString(R.string.weather_key));
            emitter.onNext(response.body().string());
        })
                //每小时只接受一次
                .throttleFirst(1, TimeUnit.HOURS)
                .filter(s -> !TextUtils.isEmpty(s))
                .doOnNext(s -> SharedPreferencesUtils.putString(MyApplication.getInstance().mContext, MyApplication.getInstance().mContext.getString(R.string.weather), s))
                .map(weather -> GsonUtils.getGsonInstance().fromJson(weather, WeatherJson.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取每日图片
     */
    public Observable<String> getPic() {
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            Response response = OkHttp.sendOkHttpRequestGet(MyApplication.getInstance().mContext.getString(R.string.pic_url));
            emitter.onNext(response.body().string());
        })
                //每天只接受一次
                .throttleFirst(1, TimeUnit.DAYS)
                .filter(s -> !TextUtils.isEmpty(s))
                .doOnNext(s -> SharedPreferencesUtils.putString(MyApplication.getInstance().mContext, MyApplication.getInstance().mContext.getString(R.string.pic), s))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
