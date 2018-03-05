package com.sorgs.sorgsweather.ui.fragment;

import com.sorgs.sorgsweather.ui.activity.MainActivity;
import com.sorgs.sorgsweather.ui.activity.WeatherActivity;
import com.sorgs.sorgsweather.http.OkHttp;
import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.db.City;
import com.sorgs.sorgsweather.db.County;
import com.sorgs.sorgsweather.db.Province;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.HandleUtility;

import org.litepal.crud.DataSupport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Sorgs
 * on 2017/5/10.
 */

public class ChooseAreaFragment extends Fragment {


    /**
     * 省级
     */
    private static final int LEVEL_PROVINCE = 0;

    /**
     * 市级
     */
    private static final int LEVEL_CITY = 1;

    /**
     * 县级
     */
    private static final int LEVEL_COUNTY = 2;

    private TextView title_text;
    private Button back_button;
    private ListView list_view;
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 当前选择的级别
     */
    private int currentLevel;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的市
     */
    private City selectedCity;


    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    private ProgressDialog progressDialog;

    /**
     * 省级是否从服务查询过
     */
    private boolean isProvinceFalge = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //把碎片加载进来
        View view = inflater.inflate(R.layout.choose_area, container, false);

        title_text = (TextView) view.findViewById(R.id.title_text);
        back_button = (Button) view.findViewById(R.id.back_button);
        list_view = (ListView) view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);

        list_view.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).weatherId;
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawer_layout.closeDrawer(GravityCompat.START);
                        activity.swipe_refresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击返回发现再根据等级去查询
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    /**
     * 查询所有的市，先从数据库查出
     */
    private void queryProvinces() {
        title_text.setText("中国");
        //这一层不能返回就，必须往下查
        back_button.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            //数据库有内容
            dataList.clear();//清除之前的数据
            for (Province province :
                    provinceList) {
                dataList.add(province.provinceName);
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);//ListView定位到指定Item的位置
            currentLevel = LEVEL_PROVINCE;//选定城市之后，把级别换位省级
        }
        if (isProvinceFalge) {
            queryFromServer(Constant.CITIES, "province");
            isProvinceFalge = false;
        }

    }

    /**
     * 查询选中省内所有的市，先从数据库查出，再去服务器更新
     */
    private void queryCities() {
        title_text.setText(selectedProvince.provinceName);
        //有市级就可以返回
        back_button.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.id)).find(City.class);
        if (cityList.size() > 0) {
            //数据库有内容
            dataList.clear();//清除之前的数据
            for (City city :
                    cityList) {
                dataList.add(city.cityName);
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);//ListView定位到指定Item的位置
            currentLevel = LEVEL_CITY;//选定城市之后，把级别换位市级
        } else {
            int provinceCode = selectedProvince.provinceCode;
            queryFromServer(Constant.CITIES + "/" + provinceCode, "city");
        }

    }

    /**
     * 查询选中市内所有的县，先从数据库查出，再去服务器更新
     */
    private void queryCounties() {
        title_text.setText(selectedCity.cityName);
        //有市级就可以返回
        back_button.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.id)).find(County.class);
        if (countyList.size() > 0) {
            //数据库有内容
            dataList.clear();//清除之前的数据
            for (County county :
                    countyList) {
                dataList.add(county.countyName);
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);//ListView定位到指定Item的位置
            currentLevel = LEVEL_COUNTY;//选定城市之后，把级别换位县级
        } else {
            int provinceCode = selectedProvince.provinceCode;
            int cityCode = selectedCity.cityCode;
            queryFromServer(Constant.CITIES + "/" + provinceCode + "/" + cityCode, "county");
        }

    }

    /**
     * 查询省市县数据
     *
     * @param address 传入地址
     * @param type    传入类型
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        OkHttp.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //切换为主线程，告知加载失败
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                boolean result = false;
                //根据类型查询
                if ("province".equals(type)) {
                    //传入解析
                    result = HandleUtility.handleProvinceResponse(string);
                } else if ("city".equals(type)) {
                    //传入解析
                    result = HandleUtility.handleCityResponse(string, selectedProvince.id);
                } else if ("county".equals(type)) {
                    //传入解析
                    result = HandleUtility.handleCountyResponse(string, selectedCity.id);
                }

                //更新UI
                if (result) {
                    //切换为主线程
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
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
            progressDialog.setCanceledOnTouchOutside(false);//dialog弹出后会点击屏幕，dialog不消失；点击物理返回键dialog消失
        }
        progressDialog.show();
    }
}
