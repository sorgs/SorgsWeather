package com.sorgs.sorgsweather.model;


import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.db.City;
import com.sorgs.sorgsweather.db.County;
import com.sorgs.sorgsweather.db.Province;
import com.sorgs.sorgsweather.http.OkHttp;
import com.sorgs.sorgsweather.ui.activity.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

public class CityViewModel extends ViewModel {


    /**
     * 从网络获取
     */
    public Observable<Boolean> requestHttp(String cityId, String type) {
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            Response response = OkHttp.sendOkHttpRequestGet(MyApplication.getInstance().mContext.getString(R.string.cities_url) + cityId);
            String s = response.body().string();
            emitter.onNext(s);

        })
                .filter(s -> !TextUtils.isEmpty(s))
                .map(s -> {
                    switch (type) {
                        case "province":
                            //解析省级
                            JSONArray jsonProvince = new JSONArray(s);
                            //把之前的旧数据清空
                            DataSupport.deleteAll(Province.class);
                            for (int i = 0; i < jsonProvince.length(); i++) {
                                JSONObject jsonObject = jsonProvince.getJSONObject(i);
                                Province province = new Province();
                                province.provinceName = jsonObject.getString("name");
                                province.id = jsonObject.getInt("id");
                                province.save();
                            }
                            return true;
                        case "city":
                            //解析市级
                            JSONArray jsonCity = new JSONArray(s);
                            for (int i = 0; i < jsonCity.length(); i++) {
                                JSONObject jsonObject = jsonCity.getJSONObject(i);
                                City city = new City();
                                city.cityName = jsonObject.getString("name");
                                city.cityCode = jsonObject.getInt("id");
                                city.provinceId = Integer.parseInt(cityId);
                                city.save();
                            }
                            return true;
                        case "county":
                            //解析县级
                            JSONArray jsonCounty = new JSONArray(s);
                            for (int i = 0; i < jsonCounty.length(); i++) {
                                JSONObject jsonObject = jsonCounty.getJSONObject(i);
                                County county = new County();
                                county.countyName = jsonObject.getString("name");
                                county.weatherId = jsonObject.getString("weather_id");
                                county.cityId = Integer.parseInt(cityId.substring(cityId.lastIndexOf("/") + 1).trim());
                                county.save();
                            }
                            return true;
                        default:
                            return false;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

}
