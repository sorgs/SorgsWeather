package com.sorgs.sorgsweather.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.utils.Constant;
import com.sorgs.sorgsweather.utils.Sputils;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!TextUtils.isEmpty(Sputils.getString(this, Constant.WEATHER, null))) {
            //是否之前选择过城市，有选择就直接跳过
            startActivity(new Intent(this, WeatherActivity.class));
            finish();
        }

    }
}
