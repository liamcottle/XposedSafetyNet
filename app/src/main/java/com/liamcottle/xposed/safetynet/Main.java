package com.liamcottle.xposed.safetynet;

import com.liamcottle.xposed.safetynet.hooks.Hook;
import com.liamcottle.xposed.safetynet.hooks.SafetyNetHook;
import com.liamcottle.xposed.safetynet.preferences.Preferences;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.ArrayList;
import java.util.List;

public class Main implements IXposedHookLoadPackage {

    private XC_LoadPackage.LoadPackageParam mLoadPackageParam;

    private void log(String message, Object... formatArgs) {
        // [XposedSafetyNet.Main] message
        message = String.format("[XposedSafetyNet.Main] %s", message);
        XposedBridge.log(String.format(message, formatArgs));
    }

    private void logWithPackage(String packageName, String message, Object... formatArgs) {
        // [XposedSafetyNet.Main] [com.example.app] message
        message = String.format("[%s] %s", packageName, message);
        log(message, formatArgs);
    }

    private List<Hook> getHooks() {
        List<Hook> hooks = new ArrayList<>();
        hooks.add(new SafetyNetHook(mLoadPackageParam));
        return hooks;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        // cache loaded package param
        mLoadPackageParam = loadPackageParam;

        // get loaded package name
        String packageName = loadPackageParam.packageName;

        // get enabled package names
        List<String> enabledPackageNames = Preferences.PACKAGE_NAMES_ENABLED.withinXposed().stringListValue();

        // if loaded package is not in list of enabled package names, do nothing
        if(!enabledPackageNames.contains(packageName)){
            return;
        }

        logWithPackage(packageName, "Loading Hooks");

        // load hooks
        for(Hook hook : getHooks()){

            try {

                logWithPackage(packageName, "Loading Hook: %s", hook.tag());

                // load hook
                hook.load();

                logWithPackage(packageName, "Hook Loaded: %s", hook.tag());

            } catch(Throwable throwable) {

                logWithPackage(packageName, "Failed to load Hook '%s': %s", hook.tag(), throwable.getMessage());
                XposedBridge.log(throwable);

            }

        }

        logWithPackage(packageName, "Hooks Loaded");

    }

}
