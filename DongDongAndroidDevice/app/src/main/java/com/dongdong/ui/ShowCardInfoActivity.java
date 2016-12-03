package com.dongdong.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dongdong.adapter.CardInfoAdapter;
import com.dongdong.adapter.ViewPagerAdapder;
import com.dongdong.base.BaseApplication;
import com.dongdong.db.LocalCardOpe;
import com.dongdong.db.RoomCardOpe;
import com.dongdong.db.entry.CardBean;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.utils.DDLog;
import com.jr.door.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 显示本地所有卡号信息界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class ShowCardInfoActivity extends Activity implements OnClickListener {

    public static final int CARD_TYPE_NATIVE = 0;
    public static final int CARD_TYPE_PLATFORM = 1;

    private Unbinder mUnbinder;

    @BindView(R.id.rb_native_card)
    RadioButton mRbNativeCard;
    @BindView(R.id.rb_platform_card)
    RadioButton mRbPlatformCard;
    @BindView(R.id.vp_show)
    ViewPager mVp;
    @BindView(R.id.tvCardCount)
    TextView mTvCardCount;
    ListView mLvNative, mLvPlatform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_card_info);
        mUnbinder = ButterKnife.bind(this);

        mRbNativeCard.setChecked(true);
        mVp.setOnPageChangeListener(new CardChangeListener());
        mRbNativeCard.setOnClickListener(this);
        mRbPlatformCard.setOnClickListener(this);

        List<View> views = new ArrayList();
        View nativeCardView = this.getLayoutInflater().inflate(R.layout.card_info, null);
        mLvNative = (ListView) nativeCardView.findViewById(R.id.lvcardInfo);
        View platformCardView = this.getLayoutInflater().inflate(R.layout.card_info, null);
        mLvPlatform = (ListView) platformCardView.findViewById(R.id.lvcardInfo);

        views.add(nativeCardView);
        views.add(platformCardView);
        mVp.setAdapter(new ViewPagerAdapder(views));
        getNativeCardInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick({R.id.iv_back, R.id.rb_native_card, R.id.rb_platform_card})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.rb_native_card:
                mVp.setCurrentItem(0);
                getNativeCardInfo();
                break;
            case R.id.rb_platform_card:
                mVp.setCurrentItem(1);
                getPlatformCardInfo();
                break;
        }
    }

    public void getNativeCardInfo() {
        List<CardBean> list = LocalCardOpe.queryAll(BaseApplication.context());
        CardInfoAdapter adapter = new CardInfoAdapter(this, list, CARD_TYPE_NATIVE);
        mLvNative.setAdapter(adapter);
        String info = getString(R.string.native_card) + getString(R.string.card_count)
                + list.size();
        mTvCardCount.setText(info);
        DDLog.i("ShowCardInfoActivity.clazz-->>getNativeCardInfo size:"
                + list.size());
    }

    public void getPlatformCardInfo() {
        List<RoomCardBean> list = RoomCardOpe.queryAll(ShowCardInfoActivity.this.getApplication());
        CardInfoAdapter adapter = new CardInfoAdapter(this, list, CARD_TYPE_PLATFORM);
        mLvPlatform.setAdapter(adapter);
        String info = getString(R.string.platform_card) + getString(R.string.card_count)
                + list.size();
        mTvCardCount.setText(info);
        DDLog.i("ShowCardInfoActivity.clazz-->>getPlatformCardInfo size:" +
                list.size());
    }

    public class CardChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case 0:
                    mRbNativeCard.setChecked(true);
                    getNativeCardInfo();
                    break;
                case 1:
                    mRbPlatformCard.setChecked(true);
                    getPlatformCardInfo();
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }

}
