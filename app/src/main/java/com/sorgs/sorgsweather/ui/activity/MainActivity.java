package com.sorgs.sorgsweather.ui.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.GetCache;
import com.sorgs.sorgsweather.utils.SharedPreferencesUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private LocationClient mLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);

        String position = getIntent().getStringExtra("position");

        if (TextUtils.isEmpty(position)) {
            if (!TextUtils.isEmpty(GetCache.getCityID(SharedPreferencesUtils.getString(getApplicationContext(), Constant.WEATHER, null)))) {
                //有缓存，不需要再去定位
                goWeather();
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
                goWeather();
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
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("weather_id", locationStr);
        startActivity(intent);
        finish();
    }

    private void goWeather() {
        startActivity(new Intent(getApplicationContext(), WeatherActivity.class));
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }


}
