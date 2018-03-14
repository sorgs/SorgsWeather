package com.sorgs.sorgsweather.ui.activity;

import com.bumptech.glide.Glide;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.BrokenLineBean;
import com.sorgs.sorgsweather.domian.WeatherJson;
import com.sorgs.sorgsweather.model.WeatherViewModel;
import com.sorgs.sorgsweather.service.AutoUpdateService;
import com.sorgs.sorgsweather.ui.widget.BrokenLineView;
import com.sorgs.sorgsweather.utils.SharedPreferencesUtils;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Sorgs
 * on 2017/5/11.
 */

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.pic_img)
    ImageView mPicImg;
    @BindView(R.id.nav_button)
    Button mNavButton;
    @BindView(R.id.title_city)
    TextView mTitleCity;
    @BindView(R.id.title_update_time)
    TextView mTitleUpdateTime;
    @BindView(R.id.degree_text)
    TextView mDegreeText;
    @BindView(R.id.iv_weather)
    ImageView mIvWeather;
    @BindView(R.id.weather_info_text)
    TextView mWeatherInfoText;
    @BindView(R.id.bl_view)
    BrokenLineView mBlView;
    @BindView(R.id.aqi_text)
    TextView mAqiText;
    @BindView(R.id.qlty_text)
    TextView mQltyText;
    @BindView(R.id.pm25_text)
    TextView mPm25Text;
    @BindView(R.id.forecast_layout)
    LinearLayout mForecastLayout;
    @BindView(R.id.comfort_text)
    TextView mComfortText;
    @BindView(R.id.car_wash_text)
    TextView mCarWashText;
    @BindView(R.id.drsg_text)
    TextView mDrsgText;
    @BindView(R.id.flu_text)
    TextView mFluText;
    @BindView(R.id.sport_text)
    TextView mSportText;
    @BindView(R.id.trav_text)
    TextView mTravText;
    @BindView(R.id.uv_text)
    TextView mUvText;
    @BindView(R.id.weather_layout)
    ScrollView mWeatherLayout;
    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.drawer_layout)
    public DrawerLayout mDrawerLayout;

    private WeatherViewModel mWeatherViewModel;
    private Unbinder mBind;
    /**
     * 获取天气是否失败
     */
    private boolean isWeatherError = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        mBind = ButterKnife.bind(this);
        initView();

    }

    private void initView() {
        mWeatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        showPic(SharedPreferencesUtils.getString(this, getString(R.string.pic), null));
        initData(getIntent().getStringExtra("weather_id"));
    }

    private void showPic(String pic) {
        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        Glide.with(getApplication())
                .load(pic)
                .into(mPicImg);
    }


    public void initData(String weatherId) {

        mWeatherViewModel.getWeather(weatherId).subscribe(this::showWeatherInfo);
        mWeatherViewModel.getPic().subscribe(this::showPic);

        mSwipeRefresh.setOnRefreshListener(() -> {
            if (isWeatherError) {
                //重新获取定位
                Intent intent = new Intent(getApplication(), LocationActivity.class);
                intent.putExtra("position", true);
                startActivity(intent);
                finish();
            } else {
                mWeatherViewModel
                        .getWeather(weatherId)
                        .subscribe(this::showWeatherInfo);
            }

            mWeatherViewModel.getPic().subscribe(this::showPic);
        });

        //开启后台更跟新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        bindService(intent, mUpDataServiceConnection, Context.BIND_AUTO_CREATE);
    }


    /**
     * 根据weatherJson数据展示天气信息
     *
     * @param weatherJson weather类的实例
     */
    private void showWeatherInfo(WeatherJson weatherJson) {


        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }

        for (WeatherJson.HeWeather5Bean heWeatherBean :
                weatherJson.getHeWeather5()) {
            if ("ok".equals(heWeatherBean.getStatus())) {

                isWeatherError = false;

                //设置城市名字
                mTitleCity.setText(heWeatherBean.getBasic().getCity());

                //设置最后更新时间
                mTitleUpdateTime.setText("更新时间: " + heWeatherBean.getBasic().getUpdate().getLoc().substring(5));

                //设置温度
                mDegreeText.setText(heWeatherBean.getNow().getTmp() + "℃");

                //设置天气
                String txt = heWeatherBean.getNow().getCond().getTxt();

                mWeatherInfoText.setText(txt);

                if (txt.contains("云")) {
                    mIvWeather.setBackgroundResource(R.mipmap.w_cloud);
                } else if (txt.contains("晴")) {
                    mIvWeather.setBackgroundResource(R.mipmap.w_sun);
                } else if (txt.contains("雪")) {
                    mIvWeather.setBackgroundResource(R.mipmap.w_snow);
                } else if (txt.contains("雨")) {
                    mIvWeather.setBackgroundResource(R.mipmap.w_rain);
                } else if (txt.contains("雷")) {
                    mIvWeather.setBackgroundResource(R.mipmap.w_thunder);
                }

                //绘制折线图
                ArrayList<BrokenLineBean> brokenLineBeans = new ArrayList<>();
                for (WeatherJson.HeWeather5Bean.HourlyForecastBean hourlyForecastBean : heWeatherBean.getHourly_forecast()) {

                    brokenLineBeans.add(new BrokenLineBean(hourlyForecastBean.getCond().getTxt()
                            , Integer.valueOf(hourlyForecastBean.getTmp())
                            , hourlyForecastBean.getDate().substring(5)));

                }
                mBlView.setData(brokenLineBeans);


                //清除之前数据
                mForecastLayout.removeAllViews();
                for (WeatherJson.HeWeather5Bean.DailyForecastBean dailyForecastBean : heWeatherBean.getDaily_forecast()) {

                    View headView = LayoutInflater.from(this).inflate(R.layout.forecast_item, mForecastLayout, false);
                    ViewHolder viewHolder = new ViewHolder(headView);

                    //预报的日期
                    viewHolder.mDateText.setText(dailyForecastBean.getDate().substring(5));

                    //预气日期的天气
                    viewHolder.mInfoText.setText(dailyForecastBean.getCond().getTxt_d());

                    //预报日期最高气温
                    viewHolder.mMaxText.setText("最高:" + dailyForecastBean.getTmp().getMax() + "℃");

                    //预报日期最低气温
                    viewHolder.mMinText.setText("最低:" + dailyForecastBean.getTmp().getMin() + "℃");

                    //风力
                    viewHolder.mWinText.setText("风力:" + dailyForecastBean.getWind().getSc());

                    //设置上去
                    mForecastLayout.addView(viewHolder.mLlRoot);
                }


                if (heWeatherBean.getAqi() != null) {
                    findViewById(R.id.ll_aqi).setVisibility(View.VISIBLE);
                    //aqi的值
                    mAqiText.setText(heWeatherBean.getAqi().getCity().getAqi());

                    //pm2.5
                    mPm25Text.setText(heWeatherBean.getAqi().getCity().getPm25());

                    mQltyText.setText(heWeatherBean.getAqi().getCity().getQlty());
                } else {
                    //隐藏这部分信息
                    findViewById(R.id.ll_aqi).setVisibility(View.GONE);
                }

                //舒适度
                mComfortText.setText(heWeatherBean.getSuggestion().getComf().getTxt());

                //洗车指数
                mCarWashText.setText(heWeatherBean.getSuggestion().getCw().getTxt());

                mDrsgText.setText(heWeatherBean.getSuggestion().getDrsg().getTxt());

                mFluText.setText(heWeatherBean.getSuggestion().getFlu().getTxt());


                //运动建议
                mSportText.setText(heWeatherBean.getSuggestion().getSport().getTxt());

                mTravText.setText(heWeatherBean.getSuggestion().getTrav().getTxt());

                mUvText.setText(heWeatherBean.getSuggestion().getUv().getTxt());

                mWeatherLayout.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                isWeatherError = true;
            }

        }


    }

    @OnClick({R.id.nav_location, R.id.nav_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.nav_location:
                //用户点击回到当前位置
                Intent intent = new Intent(getApplication(), LocationActivity.class);
                intent.putExtra("position", true);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_button:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }

    }

    ServiceConnection mUpDataServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            AutoUpdateService updateService = ((AutoUpdateService.UpDataBinder) iBinder).getService();
            updateService.setUpDataListener(weatherJson -> showWeatherInfo(weatherJson));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mBind != null) {
            mBind.unbind();
        }
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        if (mUpDataServiceConnection != null) {
            unbindService(mUpDataServiceConnection);
        }
    }


    static class ViewHolder {
        @BindView(R.id.date_text)
        TextView mDateText;
        @BindView(R.id.info_text)
        TextView mInfoText;
        @BindView(R.id.max_text)
        TextView mMaxText;
        @BindView(R.id.min_text)
        TextView mMinText;
        @BindView(R.id.win_text)
        TextView mWinText;
        @BindView(R.id.llRoot)
        LinearLayout mLlRoot;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
