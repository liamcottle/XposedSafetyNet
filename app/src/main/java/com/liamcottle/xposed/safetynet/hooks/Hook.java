package com.liamcottle.xposed.safetynet.hooks;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class Hook {

    public XC_LoadPackage.LoadPackageParam mLoadedPackageParam;

    public Hook(XC_LoadPackage.LoadPackageParam loadPackageParam){
        mLoadedPackageParam = loadPackageParam;
    }

    public abstract String tag();
    public abstract void load() throws Throwable;

    public void log(String message, Object... formatArgs) {
        message = String.format("[%s] %s", tag(), message);
        XposedBridge.log(String.format(message, formatArgs));
    }

    public ClassLoader getLoadedPackageClassLoader() {
        return mLoadedPackageParam.classLoader;
    }

    public Class<?> findClass(String className) {
        return XposedHelpers.findClass(className, getLoadedPackageClassLoader());
    }

}
