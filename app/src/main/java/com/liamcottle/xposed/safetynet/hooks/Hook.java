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
        // [XposedSafetyNet.Hook] [com.example.app] [tag] message
        message = String.format("[XposedSafetyNet.Hook] [%s] [%s] %s", mLoadedPackageParam.packageName, tag(), message);
        XposedBridge.log(String.format(message, formatArgs));
    }

    public ClassLoader getLoadedPackageClassLoader() {
        return mLoadedPackageParam.classLoader;
    }

    public Class<?> findClass(String className) {
        return XposedHelpers.findClass(className, getLoadedPackageClassLoader());
    }

}
