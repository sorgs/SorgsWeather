package com.sorgs.sorgsweather.Activity;

import android.app.Application;
import android.content.Context;

import com.bugtags.library.Bugtags;

import org.litepal.LitePal;

/**
 * 防止app崩溃，让上传错误信息
 * Created by Sorgs
 * on 2017/5/12.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        //在这里初始化
        Bugtags.start("a4b7f37e42f219b0f125024ed560807d", this, Bugtags.BTGInvocationEventNone);

        context = getApplicationContext();

        LitePal.initialize(context);

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
                System.exit(0);
            }

        });
    }
}
