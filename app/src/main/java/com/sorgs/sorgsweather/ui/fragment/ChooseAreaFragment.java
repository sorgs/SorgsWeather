package com.sorgs.sorgsweather.ui.fragment;

import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.db.City;
import com.sorgs.sorgsweather.db.County;
import com.sorgs.sorgsweather.db.Province;
import com.sorgs.sorgsweather.db.QueryCityFromDb;
import com.sorgs.sorgsweather.model.CityViewModel;
import com.sorgs.sorgsweather.ui.activity.LocationActivity;
import com.sorgs.sorgsweather.ui.activity.WeatherActivity;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.sorgs.sorgsweather.db.QueryCityFromDb.LEVEL_CITY;
import static com.sorgs.sorgsweather.db.QueryCityFromDb.LEVEL_COUNTY;
import static com.sorgs.sorgsweather.db.QueryCityFromDb.LEVEL_PROVINCE;

/**
 * Created by Sorgs
 * on 2017/5/10.
 */

public class ChooseAreaFragment extends Fragment {


    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private List<String> mCityName = new ArrayList<>();
    private List<String> mProvinceName = new ArrayList<>();
    private List<String> mCountyName = new ArrayList<>();
    private List<String> mData = new ArrayList<>();
    private List<String> mCityId = new ArrayList<>();
    private List<String> mProvinceId = new ArrayList<>();
    private List<String> mWeatherId = new ArrayList<>();

    /**
     * 当前选择的级别
     */
    private int currentLevel = LEVEL_PROVINCE;

    /**
     * 选中的省份的id
     */
    private String mSelectedProvinceId;

    /**
     * 选中的市的id
     */
    private String mSelectedCityId;


    private ProgressDialog progressDialog;

    private CityViewModel mCityViewModel;
    private String mSelectedProvinceName;
    private String mSelectedCityName;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //把碎片加载进来
        View view = inflater.inflate(R.layout.choose_area, container, false);

        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mData);

        listView.setAdapter(adapter);

        mCityViewModel = ViewModelProviders.of(this).get(CityViewModel.class);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            closeProgressDialog();
            if (currentLevel == LEVEL_PROVINCE) {
                //选定城市之后，把级别换位省级
                currentLevel = LEVEL_CITY;
                mSelectedProvinceName = mProvinceName.get(position);
                titleText.setText(mSelectedProvinceName);
                backButton.setVisibility(View.VISIBLE);
                mSelectedProvinceId = mProvinceId.get(position);
                initData(mSelectedProvinceId);
            } else if (currentLevel == LEVEL_CITY) {
                mSelectedCityName = mCityName.get(position);
                titleText.setText(mSelectedCityName);
                currentLevel = LEVEL_COUNTY;
                backButton.setVisibility(View.VISIBLE);
                mSelectedCityId = mCityId.get(position);
                initData(mSelectedCityId);
            } else if (currentLevel == LEVEL_COUNTY) {
                String weatherId = mWeatherId.get(position);
                if (getActivity() instanceof LocationActivity) {
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                } else if (getActivity() instanceof WeatherActivity) {
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.mDrawerLayout.closeDrawer(GravityCompat.START);
                    activity.mSwipeRefresh.setRefreshing(true);
                    activity.initData(weatherId);
                }

            }
        });

        backButton.setOnClickListener(v -> {
            if (currentLevel == LEVEL_COUNTY) {
                currentLevel = LEVEL_CITY;
                titleText.setText(mSelectedProvinceName);
                backButton.setVisibility(View.VISIBLE);
                initData(mSelectedCityId);
            } else if (currentLevel == LEVEL_CITY) {
                currentLevel = LEVEL_PROVINCE;
                initData(mSelectedProvinceId);
                backButton.setVisibility(View.GONE);
                titleText.setText("中国");
            }
        });

        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        initData(null);
    }

    private void initData(String cityID) {
        showProgressDialog();
        QueryCityFromDb
                .queryCity(cityID, currentLevel)
                .subscribe(object -> {
                    mData.clear();//清除之前的数据
                    switch (currentLevel) {
                        case LEVEL_PROVINCE:
                            //省级
                            mProvinceName.clear();
                            mProvinceId.clear();
                            for (Province province : (List<Province>) object) {
                                mProvinceName.add(province.provinceName);
                                mData.add(province.provinceName);
                                mProvinceId.add(String.valueOf(province.id));
                            }
                            break;
                        case LEVEL_CITY:
                            //市级
                            mCityName.clear();
                            mCityId.clear();
                            for (City city : (List<City>) object) {
                                mCityName.add(city.cityName);
                                mData.add(city.cityName);
                                mCityId.add(String.valueOf(city.cityCode));
                            }
                            break;
                        case LEVEL_COUNTY:
                            //县级
                            mCountyName.clear();
                            mWeatherId.clear();
                            for (County county : (List<County>) object) {
                                mCountyName.add(county.countyName);
                                mData.add(county.countyName);
                                //直接存储天气id
                                mWeatherId.add(county.weatherId);
                            }
                            break;
                        default:
                            break;
                    }
                    adapter.notifyDataSetChanged();
                    //ListView定位到指定Item的位置
                    listView.setSelection(0);
                    closeProgressDialog();
                }, throwable -> {
                    String type = throwable.toString().substring(throwable.toString().lastIndexOf(": ") + 1).trim();
                    //获取失败，从网络请求
                    switch (type) {
                        case LEVEL_PROVINCE + "":
                            //省级查询失败，网络获取
                            mCityViewModel
                                    .requestHttp("", "province")
                                    .subscribe(aBoolean -> {
                                        if (aBoolean) {
                                            //查询成功
                                            initData(null);
                                        }
                                    });
                            break;
                        case LEVEL_CITY + "":
                            //市级查询失败，网络获取
                            mCityViewModel
                                    .requestHttp(mSelectedProvinceId, "city")
                                    .subscribe(aBoolean -> {
                                        if (aBoolean) {
                                            //查询成功
                                            initData(mSelectedProvinceId);
                                        }
                                    });
                            break;
                        case LEVEL_COUNTY + "":
                            //县级查询失败，网络获取
                            mCityViewModel
                                    .requestHttp(mSelectedProvinceId + "/" + mSelectedCityId, "county")
                                    .subscribe(aBoolean -> {
                                        if (aBoolean) {
                                            //查询成功
                                            initData(mSelectedCityId);
                                        }
                                    });
                            break;
                        default:
                            closeProgressDialog();
                            break;
                    }

                });
    }


    /**
     * 关闭进度框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中...");
            //dialog弹出后会点击屏幕，dialog不消失；点击物理返回键dialog消失
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        closeProgressDialog();
    }


}
