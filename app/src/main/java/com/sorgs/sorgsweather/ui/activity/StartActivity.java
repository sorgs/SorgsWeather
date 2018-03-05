package com.sorgs.sorgsweather.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;

import com.sorgs.sorgsweather.R;

import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissonItem;

/**
 * Created by Sorgs
 * on 2017/5/13.
 */

public class StartActivity extends BaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        initPermisson();
    }


    /**
     * 申请权限
     */
    private void initPermisson() {
        List<PermissonItem> permissonItems = new ArrayList<PermissonItem>();


        permissonItems.add(new PermissonItem(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                "SD卡读写", R.mipmap.i_sd));

        permissonItems.add(new PermissonItem(Manifest.permission.READ_PHONE_STATE,
                "手机状态", R.mipmap.i_phone));

        permissonItems.add(new PermissonItem(Manifest.permission.ACCESS_COARSE_LOCATION,
                "地理位置", R.mipmap.i_location));

        HiPermission.create(StartActivity.this).title("亲爱的上帝")
                .permissions(permissonItems)
                .filterColor(ResourcesCompat.getColor(getResources(),
                        R.color.colorWhile, getTheme()))//图标的颜色
                .msg("为了保护世界的和平，开启这些权限吧！\n我们一起拯救世界！")
                .style(R.style.PermissionBlueStyle)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                        startActivity(new Intent(getApplication(), MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onDeny(String permisson, int position) {

                    }

                    @Override
                    public void onGuarantee(String permisson, int position) {

                    }
                });
    }


}
