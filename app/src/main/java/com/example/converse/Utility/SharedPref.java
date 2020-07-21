package com.example.converse.Utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    public static SharedPreferences.Editor getSharedPrefEditor(Context ctx) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        return sharedPreferences.edit();
    }

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
    }

}
