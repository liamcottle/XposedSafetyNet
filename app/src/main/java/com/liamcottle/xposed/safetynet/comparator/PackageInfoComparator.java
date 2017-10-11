package com.liamcottle.xposed.safetynet.comparator;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Comparator;

public class PackageInfoComparator implements Comparator<PackageInfo> {

    private PackageManager mPackageManager;

    public PackageInfoComparator(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public int compare(PackageInfo a, PackageInfo b) {

        String aLabel = a.applicationInfo.loadLabel(mPackageManager).toString();
        String bLabel = b.applicationInfo.loadLabel(mPackageManager).toString();

        return aLabel.compareToIgnoreCase(bLabel);

    }

}