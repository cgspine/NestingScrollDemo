package org.cgspine.nestscroll;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * @author cginechen
 * @date 2016-12-27
 */

public class Util {

    /**
     * DisplayMetrics
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }


    /**
     * 屏幕密度
     */
    public static float sDensity = 0f;
    public static float getDensity(Context context){
        if(sDensity == 0f){
            sDensity = getDisplayMetrics(context).density;
        }
        return sDensity;
    }

    /**
     * 单位转换: dp -> px
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp){
        return (int) (getDensity(context) * dp + 0.5);
    }

    /**
     * 单位转换:px -> dp
     * @param px
     * @return
     */
    public static int px2dp(Context context,int px){
        return (int) (px/getDensity(context) + 0.5);
    }
}
