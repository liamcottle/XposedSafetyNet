package com.liamcottle.xposed.safetynet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context mContext;

    private ArrayList<Object> mData;
    public LayoutInflater mInflater;

    public BaseRecyclerViewAdapter(Context context) {

        mContext = context;

        mData = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);

    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public void add(Object item) {
        mData.add(item);
        notifyItemInserted(getItemCount() - 1);
    }

    public void updateItem(int position, Object item) {
        mData.set(position, item);
        notifyItemChanged(position, item);
    }

    public void remove(int position){
        mData.remove(position);
        notifyDataSetChanged();
    }

    public void clear(){
        mData.clear();
        notifyDataSetChanged();
    }

    public List<Object> getData(){
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

}