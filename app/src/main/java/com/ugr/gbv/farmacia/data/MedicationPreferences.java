package com.ugr.gbv.farmacia.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ugr.gbv.farmacia.R;

public final class MedicationPreferences {

    public static void saveFirstTimeLaunch(Context context, boolean code){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String code_key = context.getString(R.string.pref_first_time_key);
        editor.putBoolean(code_key,code);
        editor.apply();
    }

    public static boolean getFirstTimeLaunch(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.pref_first_time_key),true);
    }
}
