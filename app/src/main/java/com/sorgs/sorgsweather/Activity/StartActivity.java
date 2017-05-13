package com.sorgs.sorgsweather.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;

import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissonItem;

/**
 * Created by Sorgs
 * on 2017/5/13.
 */

public class StartActivity extends Activity {


    private static final String TAG = "StartActivity";


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
                "SD卡读写", R.drawable.permission_ic_memory));

        permissonItems.add(new PermissonItem(Manifest.permission.ACCESS_COARSE_LOCATION,
                "地理位置", R.drawable.permission_ic_location));

        HiPermission.create(StartActivity.this).title("亲爱的上帝")
                .permissions(permissonItems)
                .filterColor(ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimary, getTheme()))//图标的颜色
                .msg("为了保护世界的和平，开启这些权限吧！\n我们一起拯救世界！")
                .style(R.style.PermissionBlueStyle)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        LogUtils.i(TAG, "用户关闭权限申请");
                    }

                    @Override
                    public void onFinish() {
                        LogUtils.i(TAG, "所有权限申请完毕");
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
