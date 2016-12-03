package com.dongdong.bean;

import com.dongdong.interf.CardNumCallback;

public class LocalCardBean {
    private static LocalCardBean mInstance;
    private CardNumCallback mCardNumCallback;

    private LocalCardBean() {
    }

    public static LocalCardBean getInstance() {
        if (mInstance == null) {
            synchronized (LocalCardBean.class) {
                if (mInstance == null)
                    mInstance = new LocalCardBean();
            }
        }
        return mInstance;
    }

    public boolean isSettingStatus() {
        if (mCardNumCallback == null) {
            return false;
        }
        return true;
    }

    public void findCard(String num) {
        if (mCardNumCallback != null) {
            mCardNumCallback.onFindCard(num);
        }
    }

    public void bindCallcack(CardNumCallback cardNumCallback) {
        this.mCardNumCallback = cardNumCallback;
    }

    public void unBindCallback() {
        mCardNumCallback = null;
    }
}
