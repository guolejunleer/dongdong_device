package com.dongdong.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dongdong.base.BaseApplication;
import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.utils.ProcessDataUtils;
import com.jr.door.R;

import java.util.ArrayList;
import java.util.List;

public class UnLockRecordAdapter extends BaseAdapter {

    private List<UnlockLogBean> mList = new ArrayList<>();
    private LayoutInflater mInflater;

    public UnLockRecordAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<UnlockLogBean> list) {
        mList.clear();
        for (UnlockLogBean bean : list) {
            if (bean != null) {
                mList.add(bean);
            }
        }
    }

    public List<UnlockLogBean> getData() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.unlock_record_item, parent, false);
            convertView.setTag(holder);
            holder.unLockType = (TextView) convertView.findViewById(R.id.tv_unlock_type);
            holder.unLockNumber = (TextView) convertView.findViewById(R.id.tv_unlock_number);
            holder.unLockTime = (TextView) convertView.findViewById(R.id.tv_unlock_time);
            holder.unLockUpload = (TextView) convertView.findViewById(R.id.tv_unlock_upload);
            holder.unLockRoomNum = (TextView) convertView.findViewById(R.id.tv_unlock_room_number);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UnlockLogBean bean = getData().get(position);
        holder.unLockType.setText(ProcessDataUtils.getUnlockNameByType(bean.getUnlockType(),
                BaseApplication.context()));
        holder.unLockNumber.setText(bean.getCardOrPhoneNum());
        holder.unLockTime.setText(ProcessDataUtils.getUnLockTime(bean.getUnlockTime()));
        holder.unLockUpload.setText(bean.getUpload() == 0 ?
                BaseApplication.context().getString(R.string.is_upload)
                : BaseApplication.context().getString(R.string.is_not_upload));
        holder.unLockRoomNum.setText(TextUtils.isEmpty(bean.getRoomNum())
                ? "****" : bean.getRoomNum());
        return convertView;
    }

    private static class ViewHolder {
        private TextView unLockType;
        private TextView unLockNumber;
        private TextView unLockTime;
        private TextView unLockUpload;
        private TextView unLockRoomNum;
    }


}
