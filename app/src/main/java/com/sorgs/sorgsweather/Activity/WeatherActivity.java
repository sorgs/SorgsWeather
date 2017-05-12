package com.sorgs.sorgsweather.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sorgs.sorgsweather.Http.OkHttp;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.utils.Constant;
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
    private TextView title_city, title_update_time, degree_text, weather_info_text, aqi_text, pm25_text, comfort_text, car_wash_text, sport_text;
    private LinearLayout forecast_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initUI();

        initData();
    }

    private void initData() {
        //获取缓存
        String WeatherCache = Sputils.getString(getApplicationContext(), Constant.WEATHER, null);
        if (WeatherCache != null) {
            //存在缓存，就直接去解析
            WeatherJson weatherJson = Utility.handleWeatherResponse(WeatherCache);
            showWeatherInfo(weatherJson);
        } else {
            //无缓存的时候去服务器获取
            String weatherId = getIntent().getStringExtra("weather_id");
            //请求的时候，暂时隐藏
            weather_layout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 根据城市ID去查询天气信息
     *
     * @param weatherId 传入的城市ID
     */
    private void requestWeather(final String weatherId) {
        OkHttp.sendOkHttpRequest(Constant.WEATHER_URL + weatherId + Constant.WEATHER_KEY, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                final WeatherJson weatherJson = Utility.handleWeatherResponse(string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //缓存Json数据
                        if (string != null)
                            Sputils.putString(getApplicationContext(), "weather", string);
                        showWeatherInfo(weatherJson);
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
                String cityName = heWeatherBean.getBasic().getCity();
                Log.i(TAG, "cityName: " + cityName);
                title_city.setText(cityName);

                //设置最后更新时间
                String upTime = heWeatherBean.getBasic().getUpdate().getLoc().split(" ")[1];
                Log.i(TAG, "upTime: " + upTime);
                title_update_time.setText(upTime);

                //设置温度
                String temperature = heWeatherBean.getNow().getTmp() + "℃";
                Log.i(TAG, "temperature: " + temperature);
                degree_text.setText(temperature);

                //设置天气
                String weatherInfo = heWeatherBean.getNow().getCond().getTxt();
                Log.i(TAG, "weatherInfo: " + weatherInfo);
                weather_info_text.setText(weatherInfo);


                //清除之前数据
                //forecast_layout.removeAllViews();
                for (WeatherJson.HeWeather5Bean.DailyForecastBean dailyForecastBean :
                        heWeatherBean.getDaily_forecast()) {
                    View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecast_layout, false);
                    TextView date_text = (TextView) view.findViewById(R.id.date_text);
                    TextView info_text = (TextView) view.findViewById(R.id.info_text);
                    TextView max_text = (TextView) view.findViewById(R.id.max_text);
                    TextView min_text = (TextView) view.findViewById(R.id.min_text);

                    //预报的日期
                    String date = dailyForecastBean.getDate();
                    Log.i(TAG, "date: " + date);
                    date_text.setText(date);

                    //预气日期的天气
                    String dateWeather = dailyForecastBean.getCond().getTxt_d();
                    info_text.setText(dateWeather);

                    //预报日期最高气温
                    String dateMax = dailyForecastBean.getTmp().getMax();
                    Log.i(TAG, "dateMax: " + dateMax);
                    max_text.setText(dateMax);

                    //预报日期最低气温
                    String dateMin = dailyForecastBean.getTmp().getMin();
                    Log.i(TAG, "dateMin: " + dateMin);
                    min_text.setText(dateMin);

                    //设置上去
                    forecast_layout.addView(view);
                }

                if (heWeatherBean.getAqi() != null) {
                    //aqi的值
                    String aiq = heWeatherBean.getAqi().getCity().getAqi();
                    Log.i(TAG, "aiq: " + aiq);
                    aqi_text.setText(aiq);

                    //pm2.5
                    String PM = heWeatherBean.getAqi().getCity().getPm25();
                    Log.i(TAG, "PM: " + PM);
                    pm25_text.setText(PM);
                }

                //舒适度
                String comfor = heWeatherBean.getSuggestion().getComf().getTxt();
                Log.i(TAG, "comfor: " + comfor);
                comfort_text.setText(comfor);

                //洗车指数
                String CarWash = heWeatherBean.getSuggestion().getCw().getTxt();
                Log.i(TAG, "CarWash: " + CarWash);
                car_wash_text.setText(CarWash);

                //运动建议
                String sport = heWeatherBean.getSuggestion().getSport().getTxt();
                Log.i(TAG, "sport: " + sport);
                sport_text.setText(sport);

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
        comfort_text = (TextView) findViewById(R.id.comfort_text);
        car_wash_text = (TextView) findViewById(R.id.car_wash_text);
        sport_text = (TextView) findViewById(R.id.sport_text);
    }
}
