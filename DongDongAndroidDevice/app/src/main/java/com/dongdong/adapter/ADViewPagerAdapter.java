package com.dongdong.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dongdong.utils.DDLog;
import com.jr.door.R;

public class ADViewPagerAdapter extends PagerAdapter implements
        OnPageChangeListener {

    private ViewPager mViewPager;
    private int[] mImgIdArray = new int[]{R.mipmap.test001,
            R.mipmap.test002, R.mipmap.test003};
    private ImageView[] mImageViews;

    public ADViewPagerAdapter(Context context, ViewPager viewPager) {
        mViewPager = viewPager;
        mImageViews = new ImageView[mImgIdArray.length];
        for (int i = 0; i < mImageViews.length; i++) {
            ImageView imageView = new ImageView(context);
            mImageViews[i] = imageView;
            imageView.setBackgroundResource(mImgIdArray[i]);
            DDLog.i("ADViewPagerAdapter.clazz--->>> construct add view...............");
        }
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int des = position % mImageViews.length;
        View view = mImageViews[des];
        try {
            DDLog.i("ADViewPagerAdapter.clazz--->>>destroyItem des:" + des
                    + ",position:" + position);
            container.removeView(view);
        } catch (Exception e) {
            DDLog.i("ADViewPagerAdapter.clazz--->>>destroyItem e:" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int des = position % mImageViews.length;
        View view = mImageViews[des];
        try {
            DDLog.i("ADViewPagerAdapter.clazz--->>>instantiateItem des:" + des
                    + ",position:" + position);
            container.addView(view, 0);
        } catch (Exception e) {
            DDLog.i("ADViewPagerAdapter.clazz--->>>instantiateItem e:" + e.toString());
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {
    }
}
