package com.sorgs.sorgsweather.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sorgs.sorgsweather.Http.OkHttp;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.service.AutoUpdateService;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.LogUtils;
import com.sorgs.sorgsweather.utils.Sputils;
import com.sorgs.sorgsweather.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Sorgs
 * on 2017/5/11.
 */

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weather_layout;
    private TextView title_city, title_update_time, degree_text, weather_info_text, aqi_text, pm25_text, comfort_text, car_wash_text, sport_text, qlty_text;
    private LinearLayout forecast_layout;
    private ImageView pic_img;
    public DrawerLayout drawer_layout;
    private Button nav_button;
    public SwipeRefreshLayout swipe_refresh;
    private String mWeatherId;
    private TextView air_text, drsg_text, flu_text, trav_text, uv_text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        if (Build.VERSION.SDK_INT >= 21) {
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        initUI();

        getCache();

        initData();
    }

    /**
     * 获取是否有缓存
     */
    private void getCache() {
        //获取数据缓存
        String WeatherCache = Sputils.getString(getApplicationContext(), Constant.WEATHER, null);
        Log.i(TAG, "取出缓存: " + WeatherCache);
        if (!TextUtils.isEmpty(WeatherCache)) {
            //存在缓存，就直接去解析
            WeatherJson weatherJson = Utility.handleWeatherResponse(WeatherCache);
            assert weatherJson != null;
            for (WeatherJson.HeWeatherBean heWeatherBean :
                    weatherJson.getHeWeather()) {
                if ("ok".equals(heWeatherBean.getStatus())) {
                    mWeatherId = heWeatherBean.getBasic().getCity();
                    LogUtils.i(TAG, "缓存的城市: " + mWeatherId);
                    showWeatherInfo(weatherJson);
                } else {
                    //无缓存的时候去服务器获取
                    mWeatherId = getIntent().getStringExtra("weather_id");

                    //请求的时候，暂时隐藏
                    weather_layout.setVisibility(View.INVISIBLE);
                    requestWeather(mWeatherId);
                }
            }

        } else {
            //无缓存的时候去服务器获取
            mWeatherId = getIntent().getStringExtra("weather_id");

            //请求的时候，暂时隐藏
            weather_layout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        //获取图片缓存
        String pic = Sputils.getString(getApplicationContext(), Constant.PIC, null);
        if (TextUtils.isEmpty(pic)) {
            loacdPic();
        } else {
            //设置图片
            Glide.with(getApplicationContext()).load(pic).into(pic_img);
        }
    }

    private void initData() {
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        //点击切换城市
        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_layout.openDrawer(GravityCompat.START);
            }
        });

        //开启后台更跟新服务
        startService(new Intent(this, AutoUpdateService.class));
    }

    private void loacdPic() {
        OkHttp.sendOkHttpRequest(Constant.PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pic = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplication()).load(pic).into(pic_img);
                    }
                });
            }
        });
    }

    /**
     * 根据城市ID去查询天气信息
     *
     * @param weatherId 传入的城市ID
     */
    public void requestWeather(final String weatherId) {
        OkHttp.sendOkHttpRequest(Constant.WEATHER_URL + weatherId + Constant.WEATHER_KEY, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipe_refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                final WeatherJson weatherJson = Utility.handleWeatherResponse(string);
                //缓存Json数据
                if (string != null) {
                    LogUtils.i(TAG, "缓存json: " + string);
                    Sputils.putString(getApplicationContext(), Constant.WEATHER, string);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeatherInfo(weatherJson);
                        loacdPic();
                        swipe_refresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 根据weatherJson数据展示天气信息
     *
     * @param weatherJson weather类的实例
     */
    private void showWeatherInfo(WeatherJson weatherJson) {
        for (WeatherJson.HeWeatherBean heWeatherBean :
                weatherJson.getHeWeather()) {
            if ("ok".equals(heWeatherBean.getStatus())) {
                //设置城市名字
                title_city.setText(heWeatherBean.getBasic().getCity());

                //设置最后更新时间
                title_update_time.setText("更新时间: " + heWeatherBean.getBasic().getUpdate().getLoc().substring(5));

                //设置温度
                degree_text.setText(heWeatherBean.getNow().getTmp() + "℃");

                //设置天气
                weather_info_text.setText(heWeatherBean.getNow().getCond().getTxt());


                //清除之前数据
                forecast_layout.removeAllViews();
                for (WeatherJson.HeWeatherBean.DailyForecastBean dailyForecastBean :
                        heWeatherBean.getDaily_forecast()) {
                    View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecast_layout, false);
                    TextView date_text = (TextView) view.findViewById(R.id.date_text);
                    TextView info_text = (TextView) view.findViewById(R.id.info_text);
                    TextView max_text = (TextView) view.findViewById(R.id.max_text);
                    TextView min_text = (TextView) view.findViewById(R.id.min_text);
                    TextView win_text = (TextView) view.findViewById(R.id.win_text);

                    //预报的日期
                    date_text.setText(dailyForecastBean.getDate().substring(5));

                    //预气日期的天气
                    String txt_d = dailyForecastBean.getCond().getTxt_d();
                    if (txt_d.length() <= 1) {
                        info_text.setText("天气:    " + txt_d);
                    } else {
                        info_text.setText("天气:" + txt_d);
                    }


                    //预报日期最高气温
                    String max = dailyForecastBean.getTmp().getMax();
                    if (max.length() < 2) {
                        max_text.setText("最高:    " + max + "℃");
                    } else {
                        max_text.setText("最高:" + max + "℃");
                    }


                    //预报日期最低气温

                    String min = dailyForecastBean.getTmp().getMin();
                    if (min.length() < 2) {
                        min_text.setText("最低:    " + min + "℃");
                    } else {
                        min_text.setText("最低:" + min + "℃");
                    }

                    //风力
                    win_text.setText(dailyForecastBean.getWind().getSc());

                    //设置上去
                    forecast_layout.addView(view);
                }

                if (heWeatherBean.getAqi() != null) {
                    //aqi的值
                    aqi_text.setText(heWeatherBean.getAqi().getCity().getAqi());

                    //pm2.5
                    pm25_text.setText(heWeatherBean.getAqi().getCity().getPm25());

                    qlty_text.setText(heWeatherBean.getAqi().getCity().getQlty());
                }

                air_text.setText(heWeatherBean.getSuggestion().getAir().getTxt());

                //舒适度
                comfort_text.setText(heWeatherBean.getSuggestion().getComf().getTxt());

                //洗车指数
                car_wash_text.setText(heWeatherBean.getSuggestion().getCw().getTxt());

                drsg_text.setText(heWeatherBean.getSuggestion().getDrsg().getTxt());

                flu_text.setText(heWeatherBean.getSuggestion().getFlu().getTxt());


                //运动建议
                sport_text.setText(heWeatherBean.getSuggestion().getSport().getTxt());

                trav_text.setText(heWeatherBean.getSuggestion().getTrav().getTxt());

                uv_text.setText(heWeatherBean.getSuggestion().getUv().getTxt());


                weather_layout.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
            }

        }


    }

    private void initUI() {
        weather_layout = (ScrollView) findViewById(R.id.weather_layout);
        title_city = (TextView) findViewById(R.id.title_city);
        title_update_time = (TextView) findViewById(R.id.title_update_time);
        degree_text = (TextView) findViewById(R.id.degree_text);
        weather_info_text = (TextView) findViewById(R.id.weather_info_text);
        forecast_layout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqi_text = (TextView) findViewById(R.id.aqi_text);
        pm25_text = (TextView) findViewById(R.id.pm25_text);
        qlty_text = (TextView) findViewById(R.id.qlty_text);
        comfort_text = (TextView) findViewById(R.id.comfort_text);
        car_wash_text = (TextView) findViewById(R.id.car_wash_text);
        sport_text = (TextView) findViewById(R.id.sport_text);
        air_text = (TextView) findViewById(R.id.air_text);
        drsg_text = (TextView) findViewById(R.id.drsg_text);
        flu_text = (TextView) findViewById(R.id.flu_text);
        trav_text = (TextView) findViewById(R.id.trav_text);
        uv_text = (TextView) findViewById(R.id.uv_text);
        pic_img = (ImageView) findViewById(R.id.pic_img);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_button = (Button) findViewById(R.id.nav_button);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
    }


}
