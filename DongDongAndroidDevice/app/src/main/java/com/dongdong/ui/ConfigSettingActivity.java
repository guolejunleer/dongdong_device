package com.dongdong.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;
import com.dongdong.interf.ExpandLauncherCallback;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.utils.DDLog;
import com.jr.door.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ConfigSettingActivity extends Activity implements ExpandLauncherCallback {
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
        DongDongCenter.getVisitorPicCfg();
        mCheckBoxList.add(mCbCalling);
        mCheckBoxList.add(mCbAppAnswer);
        mCheckBoxList.add(mCbPhoneAnswer);
        mCheckBoxList.add(mCbAppUnlock);
        mCheckBoxList.add(mPhoneUnlock);
        mCheckBoxList.add(mCardUnlock);
        mCheckBoxList.add(mPwdUnlock);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DeviceApplication.addExpandLauncherCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DeviceApplication.removeExpandLauncherCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }


    @Override
    public void onGetVisitorPicCfgResult(int configure) {
        DDLog.i("ConfigSettingActivity.clazz onGetVisitorPicCfgResult() configure:"
                + Integer.toBinaryString(configure));
        for (int i = 0; i < mCheckBoxList.size(); i++) {
            if ((configure & (int) Math.pow(2, i)) > 0) {
                mCheckBoxList.get(i).setChecked(true);
            } else {
                mCheckBoxList.get(i).setChecked(false);
            }
        }
    }
    @Override
    public void onSetVisitorPicCfgResult(int result) {
        if (result == APlatData.RESULT_SUCCESS) {
            BaseApplication.showToast(getString(R.string.success_setting));
        }else{
            BaseApplication.showToast(getString(R.string.fail_setting));
        }
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
                DDLog.i("ConfigSettingActivity.clazz onClick() value:"
                        + Integer.toBinaryString(value));
                DongDongCenter.setVisitorPicCfg(value);
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;

        }
    }
}
