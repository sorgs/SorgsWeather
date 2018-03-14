package com.sorgs.sorgsweather.ui.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.utils.GsonUtils;
import com.sorgs.sorgsweather.utils.SharedPreferencesUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import static com.sorgs.sorgsweather.utils.NeverAskAgainPermission.goToSettingDialog;


/**
 * @author Sorgs
 */
public class LocationActivity extends AppCompatActivity {
    private LocationClient mLocationClient;
    private boolean mPosition = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);


        mPosition = getIntent().getBooleanExtra("position", false);


        if (TextUtils.isEmpty(SharedPreferencesUtils.getString(this, getString(R.string.weather), null)) || mPosition) {
            requestPermission();
        } else {
            //有缓存,不需要再定位
            goWeather();
        }
    }

    private void requestPermission() {
        new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //尝试定位
                        initPosition();
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                        initPosition();
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        if (mPosition) {
                            //用户确实需要定位时候弹出
                            goToSettingDialog(this);
                        }
                    }
                });
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
        String weather = SharedPreferencesUtils.getString(this, getString(R.string.weather), null);
        String cityId = null;
        if (!TextUtils.isEmpty(weather)) {
            for (WeatherJson.HeWeather5Bean heWeather5Bean : GsonUtils.getGsonInstance().fromJson(weather, WeatherJson.class).getHeWeather5()) {
                if (heWeather5Bean.getStatus().equals("ok")) {
                    cityId = heWeather5Bean.getBasic().getId();
                }

            }
        }
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("weather_id", cityId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stop();
    }


}
