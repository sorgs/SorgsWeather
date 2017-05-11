package com.sorgs.sorgsweather.utils;

import android.text.TextUtils;
import android.util.Log;

import com.sorgs.sorgsweather.db.City;
import com.sorgs.sorgsweather.db.County;
import com.sorgs.sorgsweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

public class Utility {
    private static final String TAG = "Utility";

    public static boolean handleProvinceResponse(String response) {

        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonProvince = new JSONArray(response);
                //把之前的旧数据清空
                DataSupport.deleteAll(Province.class);

                for (int i = 0; i < jsonProvince.length(); i++) {
                    JSONObject jsonObject = jsonProvince.getJSONObject(i);
                    Province province = new Province();
                    Log.i(TAG, "province: " + jsonObject.getString("name"));
                    province.provinceName = jsonObject.getString("name");
                    province.provinceCode = jsonObject.getInt("id");
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonCity = new JSONArray(response);

                for (int i = 0; i < jsonCity.length(); i++) {
                    JSONObject jsonObject = jsonCity.getJSONObject(i);
                    City city = new City();
                    Log.i(TAG, "City: " + jsonObject.getString("name"));
                    city.cityName = jsonObject.getString("name");
                    city.cityCode = jsonObject.getInt("id");
                    city.provinceId = provinceId;
                    city.save();


                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonCounty = new JSONArray(response);
                for (int i = 0; i < jsonCounty.length(); i++) {
                    JSONObject jsonObject = jsonCounty.getJSONObject(i);
                    County county = new County();
                    Log.i(TAG, "County: " + jsonObject.getString("name"));
                    county.countyName = jsonObject.getString("name");
                    county.weatherId = jsonObject.getString("weather_id");
                    county.cityId = cityId;
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
