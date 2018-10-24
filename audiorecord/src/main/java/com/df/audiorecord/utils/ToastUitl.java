package com.df.audiorecord.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Danfeng on 2018/1/6.
 */

public class ToastUitl {
    private ToastUitl()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isShow = true;

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(Context context, CharSequence message)
    {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

//    /**
//     * 短时间显示Toast
//     *
//     * @param message
//     */
//    public static void showShort( int message)
//    {
//        if (isShow)
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     * 长时间显示Toast
//     *
//     * @param message
//     */
//    public static void showLong( CharSequence message)
//    {
//        if (isShow)
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//    }
//
//    /**
//     * 长时间显示Toast
//     *
//     * @param message
//     */
//    public static void showLong( int message)
//    {
//        if (isShow)
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//    }
//
//    /**
//     * 自定义显示Toast时间
//     *
//     * @param message
//     * @param duration
//     */
//    public static void show(CharSequence message, int duration)
//    {
//        if (isShow)
//            Toast.makeText(getApplicationContext(), message, duration).show();
//    }
//
//    /**
//     * 自定义显示Toast时间
//     *
//     * @param message
//     * @param duration
//     */
//    public static void show( int message, int duration)
//    {
//        if (isShow)
//            Toast.makeText(getApplicationContext(), message, duration).show();
//    }
//    public static void showWithViewLong(int layoutId){
//        if (isShow){
//            LayoutInflater inflater =LayoutInflater.from(getApplicationContext());
//            View layout=inflater.inflate(layoutId,null);
//            Toast toast = new Toast(getApplicationContext());
//            toast.setGravity(Gravity.CENTER,0,0);
//            toast.setDuration(Toast.LENGTH_LONG);
//            toast.setView(layout);
//            toast.show();
//        }
//    }
//    public static void showWithViewShort(int layoutId){
//        if (isShow){
//            LayoutInflater inflater =LayoutInflater.from(getApplicationContext());
//            View layout=inflater.inflate(layoutId,null);
//            Toast toast = new Toast(getApplicationContext());
//            toast.setGravity(Gravity.CENTER,0,0);
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.setView(layout);
//            toast.show();
//        }
//    }
}
