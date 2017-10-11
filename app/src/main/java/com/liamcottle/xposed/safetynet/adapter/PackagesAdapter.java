package com.liamcottle.xposed.safetynet.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.liamcottle.xposed.safetynet.R;
import com.liamcottle.xposed.safetynet.XposedSafetyNetApplication;

public abstract class PackagesAdapter extends BaseRecyclerViewAdapter {

    private static final int TYPE_PACKAGE = 1;

    private ClickListener mClickListener;

    public PackagesAdapter(Context context){
        super(context);
    }

    public void setClickListener(ClickListener clickListener){
        mClickListener = clickListener;
    }

    public abstract boolean isPackageSelected(PackageInfo packageInfo);

    public class PackageViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconImageView;
        private TextView nameTextView;
        private SwitchCompat enabledSwitch;

        public PackageViewHolder(View view) {

            super(view);

            iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
            nameTextView = (TextView) view.findViewById(R.id.nameTextView);
            enabledSwitch = (SwitchCompat) view.findViewById(R.id.enabledSwitch);

        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch(viewType){
            case TYPE_PACKAGE: {
                return new PackageViewHolder(inflater.inflate(R.layout.item_package, parent, false));
            }
        }

        return super.onCreateViewHolder(parent, viewType);

    }

    @Override
    public int getItemViewType(int position) {

        Object object = getItem(position);

        if(object instanceof PackageInfo){
            return TYPE_PACKAGE;
        }

        return super.getItemViewType(position);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Object object = getItem(position);
        int type = getItemViewType(position);

        switch(type){

            case TYPE_PACKAGE: {

                final PackageInfo packageInfo = (PackageInfo) object;
                PackageViewHolder packageViewHolder = (PackageViewHolder) holder;

                // get package manager
                PackageManager packageManager = XposedSafetyNetApplication.get().getPackageManager();

                // get application info
                String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);

                // set values on viewholder
                packageViewHolder.nameTextView.setText(name);
                packageViewHolder.iconImageView.setImageDrawable(icon);

                // set switch state
                packageViewHolder.enabledSwitch.setChecked(isPackageSelected(packageInfo));

                // click listener
                packageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mClickListener != null){
                            mClickListener.onPackageClick(packageInfo);
                        }
                    }
                });

                break;

            }

        }

    }

    public interface ClickListener {
        void onPackageClick(PackageInfo packageInfo);
    }

}