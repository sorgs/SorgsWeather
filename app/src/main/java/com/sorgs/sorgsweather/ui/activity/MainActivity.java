package com.sorgs.sorgsweather.ui.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.http.OkHttp;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.HandleUtility;
import com.sorgs.sorgsweather.utils.Sputils;
import com.sorgs.sorgsweather.utils.GetCache;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends BaseActivity {
    private LocationClient mLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);


        String position = getIntent().getStringExtra("position");

        if (TextUtils.isEmpty(position)) {
            if (!TextUtils.isEmpty(GetCache.getCityID(Sputils.getString(getApplicationContext(), Constant.WEATHER, null)))) {
                //有缓存，不需要再去定位
                GoWeather();
            } else {
                //尝试定位
                initPosition();
            }
        } else {
            //用户点击回到当前定位地址
            //尝试定位
            initPosition();
        }
    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            String mLocation = location.getLongitude() + "," + location.getLatitude();
            if (!TextUtils.isEmpty(mLocation)) {
                //不为空,显示地理位置经纬度F
                sendService(mLocation);
            } else {
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_SHORT).show();
                //进入主页
                GoWeather();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }


    /**
     * 获取地理位置
     */
    private void initPosition() {
        mLocationClient.start();
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
                HandleUtility.handleWeatherResponse(string);
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
        if (!TextUtils.isEmpty(WeatherCache)) {
            //存在缓存，就去尝试解析
            WeatherJson weatherJson = HandleUtility.handleWeatherResponse(WeatherCache);
            assert weatherJson != null;
            for (WeatherJson.HeWeather5Bean heWeatherBean :
                    weatherJson.getHeWeather5()) {
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }


}
