package com.paul.t41popmovies.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Zheng-rt on 2017/12/11.
 *
 * 提供用户偏好的设置，重置，获取 功能。
 */



public final class MoviePreferences {
    public static final String PREFER_LOADER_ID = "pref_loader_id";
    public static void setPreferLoaderId(Context context, int loderId){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(PREFER_LOADER_ID, loderId);
        editor.apply();
    }
    public static void resetPreferLoaderId(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(PREFER_LOADER_ID);
        editor.apply();
    }
    public static int getPreferLoaderId(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final int DEFAULT_LOADER_ID = 0;//默认是0 那个loader，去载入最流行
        return sp.getInt(PREFER_LOADER_ID, DEFAULT_LOADER_ID);
    }
}
