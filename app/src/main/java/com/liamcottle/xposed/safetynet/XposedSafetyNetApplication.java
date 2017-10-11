package com.liamcottle.xposed.safetynet;

import android.app.Application;

public class XposedSafetyNetApplication extends Application {

    private static XposedSafetyNetApplication sInstance;

    @Override
    public void onCreate() {

        super.onCreate();

        sInstance = this;

    }

    public static XposedSafetyNetApplication get() {
        return sInstance;
    }

}
