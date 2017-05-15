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
import android.view.View;
import android.widget.RelativeLayout;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
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

    /**
     * 用于判断是否可以跳过广告，进入主页面
     */
    private boolean canJump;


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

        permissonItems.add(new PermissonItem(Manifest.permission.ACCESS_COARSE_LOCATION,
                "地理位置", R.mipmap.i_location));

        permissonItems.add(new PermissonItem(Manifest.permission.READ_PHONE_STATE,
                "手机状态", R.mipmap.i_phone));

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
                        requestAds();
                    }

                    @Override
                    public void onDeny(String permisson, int position) {

                    }

                    @Override
                    public void onGuarantee(String permisson, int position) {

                    }
                });
    }

    /**
     * 请求广告
     */
    private void requestAds() {

        RelativeLayout rl_start = (RelativeLayout) findViewById(R.id.rl_start);
        //应用ID
        String appId = "1106090703";
        String adId = "";
        new SplashAD(this, rl_start, appId, adId, new SplashADListener() {
            @Override
            public void onADDismissed() {
                //广告展示完毕
            }

            @Override
            public void onNoAD(int i) {
                //广告加载失败
                forWard();
            }

            @Override
            public void onADPresent() {
                //广告加载成功
            }

            @Override
            public void onADClicked() {
                //广告被点击
            }

            @Override
            public void onADTick(long l) {

            }
        });
    }

    private void forWard() {
        if (canJump) {
            startActivity(new Intent(getApplication(), MainActivity.class));
            finish();
        } else {
            canJump = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            forWard();
        }
        canJump = true;
    }
}
