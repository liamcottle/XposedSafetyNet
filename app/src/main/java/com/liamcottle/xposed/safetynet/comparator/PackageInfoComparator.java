package com.liamcottle.xposed.safetynet.comparator;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public class PackageInfoComparator implements Comparator<PackageInfo> {

    private PackageManager mPackageManager;

    public PackageInfoComparator(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public int compare(PackageInfo a, PackageInfo b) {

        String aLabel = null;
        String bLabel = null;

        try {
            aLabel = a.applicationInfo.loadLabel(mPackageManager).toString();
        } catch(Exception ignore) {}

        try {
            bLabel = b.applicationInfo.loadLabel(mPackageManager).toString();
        } catch(Exception ignore) {}

        return StringUtils.compareIgnoreCase(aLabel, bLabel);

    }

}