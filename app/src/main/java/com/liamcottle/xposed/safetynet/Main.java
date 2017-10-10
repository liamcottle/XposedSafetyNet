package com.liamcottle.xposed.safetynet;

import com.liamcottle.xposed.safetynet.hooks.Hook;
import com.liamcottle.xposed.safetynet.hooks.SafetyNetHook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.ArrayList;
import java.util.List;

public class Main implements IXposedHookLoadPackage {

    private XC_LoadPackage.LoadPackageParam mLoadPackageParam;

    /**
     * @return list of package names that the hooks should be loaded in
     */
    private List<String> getWhiteListedPackageNames() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("com.snapchat.android");
        packageNames.add("com.scottyab.safetynet.sample");
        return packageNames;
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

        // if loaded package is not whitelisted, do nothing
        if(!getWhiteListedPackageNames().contains(mLoadPackageParam.packageName)){
            return;
        }

        // load hooks
        for(Hook hook : getHooks()){

            try {

                // load hook
                hook.load();

            } catch(Throwable throwable) {

                // failed to load hook
                throwable.printStackTrace();

            }

        }

    }

}
