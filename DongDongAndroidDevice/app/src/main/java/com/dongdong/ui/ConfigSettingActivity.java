package com.dongdong.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.dongdong.base.BaseApplication;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.SPUtils;
import com.jr.door.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ConfigSettingActivity extends Activity {
    private Unbinder mUnBinder;
    @BindView(R.id.cb_calling)
    CheckBox mCbCalling;
    @BindView(R.id.cb_app_answer)
    CheckBox mCbAppAnswer;
    @BindView(R.id.cb_phone_answer)
    CheckBox mCbPhoneAnswer;
    @BindView(R.id.cb_app_unlock)
    CheckBox mCbAppUnlock;
    @BindView(R.id.cb_phone_unlock)
    CheckBox mPhoneUnlock;
    @BindView(R.id.cb_card_unlock)
    CheckBox mCardUnlock;
    @BindView(R.id.cb_pwd_unlock)
    CheckBox mPwdUnlock;
    @BindView(R.id.bt_sure)
    Button mBtSure;
    @BindView(R.id.iv_back)
    ImageView mIvBack;
    List<CheckBox> mCheckBoxList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_config_setting);
        mUnBinder = ButterKnife.bind(this);
        mCheckBoxList.add(mCbCalling);
        mCheckBoxList.add(mCbAppAnswer);
        mCheckBoxList.add(mCbPhoneAnswer);
        mCheckBoxList.add(mCbAppUnlock);
        mCheckBoxList.add(mPhoneUnlock);
        mCheckBoxList.add(mCardUnlock);
        mCheckBoxList.add(mPwdUnlock);

        Integer photoMode = (Integer) SPUtils.getParam(BaseApplication.context(),
                SPUtils.PHOTO_MODE_CONFIG_SHARE_PREF_NAME, SPUtils.SP_PHOTO_MODE_KEY, 7);

        DDLog.i("ConfigSettingActivity.clazz--->>>photoMode:" + photoMode);
        for (int i = 0; i < mCheckBoxList.size(); i++) {
            if ((photoMode & (int) Math.pow(2, i)) > 0) {
                mCheckBoxList.get(i).setChecked(true);
            } else {
                mCheckBoxList.get(i).setChecked(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }

    @OnClick({R.id.iv_back, R.id.bt_sure})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_sure:
                int value = 0;
                for (int i = 0; i < mCheckBoxList.size(); i++) {
                    if (mCheckBoxList.get(i).isChecked()) {
                        value += Math.pow(2, i);
                    }
                }
                DDLog.i("ConfigSettingActivity.clazz onClick() value:" + value
                        + ",toBinaryString:" + Integer.toBinaryString(value));
                BaseApplication.showToast(getString(R.string.success_setting));
                SPUtils.setParam(BaseApplication.context(),
                        SPUtils.PHOTO_MODE_CONFIG_SHARE_PREF_NAME,
                        SPUtils.SP_PHOTO_MODE_KEY, value);
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;

        }
    }
}
