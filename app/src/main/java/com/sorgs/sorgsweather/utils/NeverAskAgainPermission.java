package com.sorgs.sorgsweather.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

/**
 * description: xxx.
 *
 * @author Sorgs.
 * @date 2018/2/23.
 */

public class NeverAskAgainPermission {

    /**
     * 弹出跳转设置dialog
     */
    public static void goToSettingDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage("您好，我们需要您开启权限申请，才能方便使用！：\n请点击前往设置页面\n")
                .setPositiveButton("前往设置页面", (dialog, which) -> gotoSettings(activity))
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 跳转设置页面
     */
    private static void gotoSettings(Activity activity) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
