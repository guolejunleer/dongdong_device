package com.dongdong.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dongdong.db.entry.BulletinBean;
import com.jr.door.R;

import java.util.List;

public class BulletinAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //返回View的类型
    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_NORMAL_ITEM = 1;

    private Context mContext;
    private List<BulletinBean> mData;
    private final LayoutInflater mLayoutInflater;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public BulletinAdapter(Context context, List<BulletinBean> data) {
        this.mData = data;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {return mData.size() == 0 ? 1 : mData.size();}

    @Override
    public int getItemViewType(int position) {
        if (mData.size() == 0) {
            return TYPE_EMPTY;
        } else {
            return TYPE_NORMAL_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            return new EmptyViewHolder(mLayoutInflater.inflate(
                    R.layout.empty_view, parent, false));
        } else {
            return new NormalItemHolder(mLayoutInflater.inflate(
                    R.layout.bulletin_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NormalItemHolder) {
            NormalItemHolder normalItemHolder = (NormalItemHolder) holder;
            normalItemHolder.mTvTitle.setText(mData.get(position).getTitle());
            normalItemHolder.mTvTitle.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            normalItemHolder.mTvCreated.setText(mData.get(position).getCreated());

            if (onItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, position);
                    }
                });
            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).mTvEmpty.setText(mContext.getString(R.string.no_bulletin));
        }
    }

    //空数据布局
    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvEmpty;

        EmptyViewHolder(View view) {
            super(view);
            mTvEmpty = (TextView) view.findViewById(R.id.tv_empty_text);
        }
    }

    //填充数据布局
    private class NormalItemHolder extends RecyclerView.ViewHolder {
        TextView mTvTitle;
        TextView mTvCreated;

        NormalItemHolder(View view) {
            super(view);
            mTvTitle = (TextView) view.findViewById(R.id.tv_title);
            mTvCreated = (TextView) view.findViewById(R.id.tv_created);
        }
    }
}
