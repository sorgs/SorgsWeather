package com.sorgs.sorgsweather.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * description: xxx.
 *
 * @author Sorgs.
 * @date 2018/2/28.
 */

public class ScreenUtils {
    public static int dp2px(Context c, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context c, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }

    public static float dp2pxF(Context c, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static float sp2pxF(Context c, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }
}
