package com.liamcottle.xposed.safetynet.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.liamcottle.xposed.safetynet.R;
import com.liamcottle.xposed.safetynet.adapter.PackagesAdapter;
import com.liamcottle.xposed.safetynet.comparator.PackageInfoComparator;
import com.liamcottle.xposed.safetynet.preferences.Preferences;

import java.util.Collections;
import java.util.List;

public class PackagesFragment extends XposedSafetyNetFragment {

    private Activity mActivity;

    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;
    private ProgressBar mProgressBar;

    private PackagesAdapter mPackagesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_packages, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mEmptyTextView = (TextView) view.findViewById(android.R.id.empty);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);

        mPackagesAdapter = new PackagesAdapter(mActivity) {
            @Override
            public boolean isPackageSelected(PackageInfo packageInfo) {
                return Preferences.PACKAGE_NAMES_ENABLED.withinSelf().stringListValue().contains(packageInfo.packageName);
            }
        };

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mPackagesAdapter);

        mPackagesAdapter.setClickListener(new PackagesAdapter.ClickListener() {
            @Override
            public void onPackageClick(PackageInfo packageInfo) {

                String packageName = packageInfo.packageName;
                List<String> packageNamesEnabled = Preferences.PACKAGE_NAMES_ENABLED.withinSelf().stringListValue();

                if(packageNamesEnabled.contains(packageName)){

                    // remove from enabled package names
                    packageNamesEnabled.remove(packageName);

                } else {

                    // add to enabled package names
                    packageNamesEnabled.add(packageName);

                }

                // update package names enabled
                Preferences.PACKAGE_NAMES_ENABLED.withinSelf().set(packageNamesEnabled);

                // update adapter
                mPackagesAdapter.notifyDataSetChanged();

                // ask to kill package's process
                askToKillPackageProcess(packageName);

            }
        });

        loadPackages();

        return view;

    }

    private void killProcess(String packageName) {

        // get activity manager
        ActivityManager activityManager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);

        // kill processes by package name
        activityManager.killBackgroundProcesses(packageName);

    }

    private void launchPackage(String packageName) {

        try {
            startActivity(mActivity.getPackageManager().getLaunchIntentForPackage(packageName));
        } catch(Exception ignore){}

    }

    private void askToKillPackageProcess(final String packageName) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title(R.string.title_kill_process);
        builder.content(R.string.info_kill_process);
        builder.positiveText(R.string.button_kill_process);
        builder.negativeText(R.string.button_kill_process_and_launch);
        builder.neutralText(R.string.button_dismiss);
        builder.btnStackedGravity(GravityEnum.CENTER);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                killProcess(packageName);
            }
        });
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                killProcess(packageName);
                launchPackage(packageName);
            }
        });
        showDialog(builder.build());

    }

    private void loadPackages() {

        // show loading
        showLoading(true);

        // get and sort packages in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                // get package manager
                PackageManager packageManager = mActivity.getPackageManager();

                // get packages
                final List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

                // sort packages
                Collections.sort(installedPackages, new PackageInfoComparator(packageManager));

                // run on ui thread
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // clear adapter
                        mPackagesAdapter.clear();

                        // iterate packages
                        for(PackageInfo packageInfo : installedPackages){

                            // add to adapter
                            mPackagesAdapter.add(packageInfo);

                        }

                        // hide loading
                        showLoading(false);

                    }
                });

            }
        }).start();

    }

    private void showLoading(boolean loading){

        // show progress bar if loading and adapter is empty
        mProgressBar.setVisibility(loading && mPackagesAdapter.isEmpty() ? View.VISIBLE : View.GONE);

        // show empty view if not loading and adapter is empty
        if(!loading && mPackagesAdapter.isEmpty()){
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
        }

    }

}
