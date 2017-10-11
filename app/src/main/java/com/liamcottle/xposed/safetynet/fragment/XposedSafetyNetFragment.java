package com.liamcottle.xposed.safetynet.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class XposedSafetyNetFragment extends Fragment {

    public Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    public void showDialog(Dialog dialog){
        try {
            dialog.show();
        } catch(Exception ignore){}
    }

    public void dismissDialog(Dialog dialog){
        try {
            dialog.dismiss();
        } catch(Exception ignore){}
    }

}
