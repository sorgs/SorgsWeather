package com.sorgs.sorgsweather.Activity;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.sorgs.sorgsweather.utils.LogUtils;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * 防止app崩溃，让上传错误信息
 * Created by Sorgs
 * on 2017/5/12.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        LitePal.initialize(context);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                //在获取到了未捕获的异常后,处理的方法
                e.printStackTrace();
                LogUtils.i(TAG, "捕获到了一个程序的异常");
                //将捕获的异常存储到 sd卡中
                String path = context.getCacheDir() + "/error.xml";
                File file = new File(path);
                try {
                    PrintWriter printWriter = new PrintWriter(file);
                    e.printStackTrace(printWriter);
                    printWriter.close();
                    //上传服务器
                    //...
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                //结束应用
                //System.exit(0);
            }

        });
    }
}
