package com.dongdong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dongdong.db.entry.CardBean;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.ui.ShowCardInfoActivity;
import com.jr.door.R;

import java.util.List;

/**
 * 卡号信息适配器
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class CardInfoAdapter extends BaseAdapter {

    private Context mContext;
    private List<Object> mList;
    private LayoutInflater mInflater;
    private int mCardType;


    public CardInfoAdapter(Context context, List list, int cardType) {
        this.mContext = context;
        this.mList = list;
        mCardType = cardType;
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.card_info_item, null);
            convertView.setTag(holder);
            holder.cardNum = (TextView) convertView
                    .findViewById(R.id.tv_card_num);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String cardNum = "";
        if (mCardType == ShowCardInfoActivity.CARD_TYPE_NATIVE) {
            cardNum = ((CardBean) mList.get(position)).getCardNum();
            cardNum = String.format(mContext.getString(R.string.native_card_num), position,
                    cardNum);
        } else if (mCardType == ShowCardInfoActivity.CARD_TYPE_PLATFORM) {
            cardNum = ((RoomCardBean) mList.get(position)).getCardNum();
            cardNum = String.format(mContext.getString(R.string.platform_card_num), position,
                    cardNum);
        }
        holder.cardNum.setText(cardNum);
        return convertView;
    }

    static class ViewHolder {
        private TextView cardNum;
    }

}
