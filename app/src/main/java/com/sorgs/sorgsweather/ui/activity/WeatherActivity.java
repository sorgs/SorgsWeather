package com.sorgs.sorgsweather.ui.activity;

import com.bumptech.glide.Glide;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.BrokenLineBean;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.http.OkHttp;
import com.sorgs.sorgsweather.service.AutoUpdateService;
import com.sorgs.sorgsweather.ui.widget.BrokenLineView;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.HandleUtility;
import com.sorgs.sorgsweather.utils.Sputils;
import com.sorgs.sorgsweather.utils.GetCache;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Sorgs
 * on 2017/5/11.
 */

public class WeatherActivity extends BaseActivity {


    private ScrollView weather_layout;
    private TextView title_city, title_update_time, degree_text, weather_info_text, aqi_text, pm25_text, comfort_text, car_wash_text, sport_text, qlty_text;
    private LinearLayout forecast_layout;
    private ImageView pic_img;
    public DrawerLayout drawer_layout;
    private Button nav_button;
    public SwipeRefreshLayout swipe_refresh;
    private TextView drsg_text, flu_text, trav_text, uv_text;
    private BrokenLineView bl_view;
    private ImageView weatherIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= 21) {
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        initUI();

        getCache();

        initData();
    }

    /**
     * 获取是否有缓存
     */
    private void getCache() {
        //获取缓存的json
        String WeatherCache = Sputils.getString(getApplicationContext(), Constant.WEATHER, null);
        //获取城市id
        String cityID = GetCache.getCityID(WeatherCache);
        if (!TextUtils.isEmpty(cityID)) {
            //存在缓存并且正确，就直接去解析
            WeatherJson weatherJson = HandleUtility.handleWeatherResponse(WeatherCache);
            showWeatherInfo(weatherJson);
        } else {
            //无缓存的时候去服务器获取

            //获取传过来的id
            String WeatherId = getIntent().getStringExtra("weather_id");
            //请求的时候，暂时隐藏
            weather_layout.setVisibility(View.INVISIBLE);
            requestWeather(WeatherId);
        }

        //获取图片缓存
        String pic = Sputils.getString(getApplicationContext(), Constant.PIC, null);
        if (TextUtils.isEmpty(pic)) {
            loadPic();
        } else {
            //设置图片
            Glide.with(getApplicationContext()).load(pic).into(pic_img);
        }
    }

    private void initData() {
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                requestWeather(GetCache.getCityID(Sputils.getString(getApplicationContext(),
                        Constant.WEATHER, null)));
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

        //用户点击回到当前位置
        findViewById(R.id.nav_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击开始定位，回到自己定位位置
                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.putExtra("position", "true");
                startActivity(intent);
                finish();
            }
        });
    }


    private void loadPic() {
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
                final String string = response.body().string();
                //是否能获取城市id 缓存Json数据
                if (!TextUtils.isEmpty(GetCache.getCityID(string))) {
                    Sputils.putString(getApplicationContext(), Constant.WEATHER, string);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WeatherJson weatherJson = HandleUtility.handleWeatherResponse(string);
                        showWeatherInfo(weatherJson);
                        loadPic();
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

        for (WeatherJson.HeWeather5Bean heWeatherBean :
                weatherJson.getHeWeather5()) {
            if ("ok".equals(heWeatherBean.getStatus())) {
                //设置城市名字
                title_city.setText(heWeatherBean.getBasic().getCity());

                //设置最后更新时间
                title_update_time.setText("更新时间: " + heWeatherBean.getBasic().getUpdate().getLoc().substring(5));

                //设置温度
                degree_text.setText(heWeatherBean.getNow().getTmp() + "℃");

                //设置天气
                String txt = heWeatherBean.getNow().getCond().getTxt();

                weather_info_text.setText(txt);


                if (txt.contains("云")) {
                    weatherIcon.setBackgroundResource(R.mipmap.w_cloud);
                } else if (txt.contains("晴")) {
                    weatherIcon.setBackgroundResource(R.mipmap.w_sun);
                } else if (txt.contains("雪")) {
                    weatherIcon.setBackgroundResource(R.mipmap.w_snow);
                } else if (txt.contains("雨")) {
                    weatherIcon.setBackgroundResource(R.mipmap.w_rain);
                } else if (txt.contains("雷")) {
                    weatherIcon.setBackgroundResource(R.mipmap.w_thunder);
                }

                //绘制折线图
                ArrayList<BrokenLineBean> brokenLineBeans = new ArrayList<>();
                for (WeatherJson.HeWeather5Bean.HourlyForecastBean hourlyForecastBean : heWeatherBean.getHourly_forecast()) {

                    brokenLineBeans.add(new BrokenLineBean(hourlyForecastBean.getCond().getTxt()
                            , Integer.valueOf(hourlyForecastBean.getTmp())
                            , hourlyForecastBean.getDate().substring(5)));

                }
                bl_view.setData(brokenLineBeans);


                //清除之前数据
                forecast_layout.removeAllViews();
                for (WeatherJson.HeWeather5Bean.DailyForecastBean dailyForecastBean :
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
                    info_text.setText(dailyForecastBean.getCond().getTxt_d());

                    //预报日期最高气温
                    max_text.setText("最高:" + dailyForecastBean.getTmp().getMax() + "℃");

                    //预报日期最低气温
                    min_text.setText("最低:" + dailyForecastBean.getTmp().getMin() + "℃");

                    //风力
                    win_text.setText("风力:" + dailyForecastBean.getWind().getSc());

                    //设置上去
                    forecast_layout.addView(view);
                }

                if (heWeatherBean.getAqi() != null) {
                    findViewById(R.id.ll_aqi).setVisibility(View.VISIBLE);
                    //aqi的值
                    aqi_text.setText(heWeatherBean.getAqi().getCity().getAqi());

                    //pm2.5
                    pm25_text.setText(heWeatherBean.getAqi().getCity().getPm25());

                    qlty_text.setText(heWeatherBean.getAqi().getCity().getQlty());
                } else {
                    //隐藏这部分信息
                    findViewById(R.id.ll_aqi).setVisibility(View.GONE);
                }

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
        drsg_text = (TextView) findViewById(R.id.drsg_text);
        flu_text = (TextView) findViewById(R.id.flu_text);
        trav_text = (TextView) findViewById(R.id.trav_text);
        uv_text = (TextView) findViewById(R.id.uv_text);
        pic_img = (ImageView) findViewById(R.id.pic_img);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_button = (Button) findViewById(R.id.nav_button);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        //折线图
        bl_view = (BrokenLineView) findViewById(R.id.bl_view);

        weatherIcon = (ImageView) findViewById(R.id.iv_weather);
    }

}
