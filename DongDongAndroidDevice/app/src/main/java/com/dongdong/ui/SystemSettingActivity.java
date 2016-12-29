package com.dongdong.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jr.door.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SystemSettingActivity extends Activity implements View.OnClickListener {

    private Unbinder mUnBinder;
    @BindView(R.id.ll_user_setting)
    LinearLayout mLlUserSetting;
    @BindView(R.id.ll_cfg_setting)
    LinearLayout mLlCfgSetting;
    @BindView(R.id.iv_back)
    ImageView mIvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.system_setting_avtivity);
        mUnBinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }

    @OnClick({R.id.ll_user_setting, R.id.ll_cfg_setting, R.id.iv_back})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_user_setting:
                this.startActivity(new Intent(this, UserSettingActivity.class));
                break;
            case R.id.ll_cfg_setting:
                this.startActivity(new Intent(this, ConfigSettingActivity.class));
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }

    }
}
