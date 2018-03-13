package com.sorgs.sorgsweather.ui.activity;

import org.litepal.LitePal;

import android.app.Application;
import android.content.Context;

/**
 * 防止app崩溃，让上传错误信息
 * Created by Sorgs
 * on 2017/5/12.
 */

public class MyApplication extends Application {

    public Context mContext;
    private static MyApplication mApplication;

    public static MyApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        mApplication = this;

        mContext = getApplicationContext();

        LitePal.initialize(mContext);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                //在获取到了未捕获的异常后,处理的方法
                e.printStackTrace();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                //System.exit(0);
            }

        });
    }


}
