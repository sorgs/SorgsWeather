package com.sorgs.sorgsweather.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.sorgs.sorgsweather.Http.OkHttp;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.LogUtils;
import com.sorgs.sorgsweather.utils.Sputils;
import com.sorgs.sorgsweather.utils.Utility;
import com.sorgs.sorgsweather.utils.getCache;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String position = getIntent().getStringExtra("position");

        if (TextUtils.isEmpty(position)) {
            if (!TextUtils.isEmpty(getCache.getCityID(Sputils.getString(getApplicationContext(), Constant.WEATHER, null)))) {
                //有缓存，不需要再去定位
                LogUtils.i(TAG, "M获取缓存城市：" + getCache.getCityID(Sputils.getString(getApplicationContext(), Constant.WEATHER, null)));
                GoWeather();
            } else {
                //尝试定位
                initPosition();
            }
        } else {
            //用户点击回到当前定位地址
            //尝试定位
            LogUtils.i(TAG, "尝试定位");
            initPosition();
        }


    }


    /**
     * 获取地理位置
     */
    private void initPosition() {
        //获取地理位置管理器  
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String locationProvider = LocationManager.NETWORK_PROVIDER;

        try {
            new Thread().sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //获取Location
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            //不为空,显示地理位置经纬度
            showLocation(location);
        } else {
            Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_SHORT).show();
            //进入主页
            GoWeather();
        }
        //监视地理位置变化  
        locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
    }


    /**
     * 显示地理位置经度和纬度信息
     *
     * @param location 传入地理位置
     */
    private void showLocation(Location location) {
        String locationStr = location.getLongitude() + "," + location.getLatitude();
        LogUtils.i(TAG, locationStr);
        if (!TextUtils.isEmpty(locationStr)) {
            //请求地理位置的天气信息
            sendService(locationStr);
        } else {
            Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_SHORT).show();
            //进入主页
            GoWeather();
        }

    }

    /**
     * 请求当前位置的天气
     *
     * @param locationStr 传入的经纬度
     */
    private void sendService(String locationStr) {
        OkHttp.sendOkHttpRequest(Constant.WEATHER_URL + locationStr + Constant.WEATHER_KEY, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_SHORT).show();
                        //进入主页
                        GoWeather();

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                Utility.handleWeatherResponse(string);
                //缓存Json数据
                if (string != null) {
                    Sputils.putString(getApplicationContext(), Constant.WEATHER, string);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //进入主页
                        GoWeather();
                    }
                });
            }
        });

    }

    private void GoWeather() {

        //获取数据缓存
        String WeatherCache = Sputils.getString(getApplicationContext(), Constant.WEATHER, null);
        LogUtils.i(TAG, "M取出缓存" + WeatherCache);
        if (!TextUtils.isEmpty(WeatherCache)) {
            //存在缓存，就去尝试解析
            WeatherJson weatherJson = Utility.handleWeatherResponse(WeatherCache);
            assert weatherJson != null;
            for (WeatherJson.HeWeatherBean heWeatherBean :
                    weatherJson.getHeWeather()) {
                if ("ok".equals(heWeatherBean.getStatus())) {
                    //是否之前选择过城市，有选择就直接跳过
                    startActivity(new Intent(getApplicationContext(), WeatherActivity.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "定位失败，请选择城市", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }


    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新显示
            showLocation(location);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //移除监听器
            locationManager.removeUpdates(locationListener);
        }
    }


}
