package com.dongdong.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.dongdong.adapter.ADViewPagerAdapter;
import com.dongdong.utils.DDLog;

public class AdViewPager extends ViewPager {

    private PagerAdapter adPagerAdapter;

    public AdViewPager(Context context) {
        super(context);
    }

    public AdViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void showUI() {
        adPagerAdapter = new ADViewPagerAdapter(getContext(), this);
        this.setAdapter(adPagerAdapter);
        this.setCurrentItem(10000);
        DDLog.i("AdViewPager.clazz--->>> showUI...............");
    }
}
