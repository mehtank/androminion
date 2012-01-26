package com.mehtank.androminion.ui;

import android.content.Context;

public class Strings {
    public static String format(String str, Object... args) {
        return String.format(str, args);
    }

    public static String format(Context context, int resId, Object... args) {
        return String.format(context.getString(resId), args);
    }
    
    public static String getString(Context context, int resId) {
        return context.getString(resId);
    }
}