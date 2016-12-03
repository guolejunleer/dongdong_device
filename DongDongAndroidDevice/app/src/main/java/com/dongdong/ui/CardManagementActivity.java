package com.dongdong.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.jr.door.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 卡号管理界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class CardManagementActivity extends Activity {

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.card_management_activity);
        mUnbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick({R.id.iv_regiset_card, R.id.iv_cancel_card, R.id.iv_quere_card,
            R.id.iv_verify_card, R.id.iv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_regiset_card:
                this.startActivity(new Intent(this, RegisterCardActivity.class));
                break;
            case R.id.iv_cancel_card:
                this.startActivity(new Intent(this, UnregisterCardActivity.class));
                break;
            case R.id.iv_quere_card:
                this.startActivity(new Intent(this, ShowCardInfoActivity.class));
                break;
            case R.id.iv_verify_card:
                this.startActivity(new Intent(this, VerifyCardActivity.class));
                break;
            case R.id.iv_back:
                this.finish();
                break;
        }
    }
}
