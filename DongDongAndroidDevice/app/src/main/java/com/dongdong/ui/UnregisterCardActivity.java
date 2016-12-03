package com.dongdong.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.dongdong.widget.SearchDevicesView;
import com.dongdong.base.BaseApplication;
import com.dongdong.bean.LocalCardBean;
import com.dongdong.db.LocalCardOpe;
import com.dongdong.db.entry.CardBean;
import com.dongdong.interf.CardNumCallback;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.jr.door.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 注销卡号界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class UnregisterCardActivity extends Activity implements CardNumCallback {

    private Unbinder mUnbinder;
    
    @BindView(R.id.search_device_view)
    SearchDevicesView mSerarchCardView;
    private CommonDialog mCommonDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.card_react_activity_main);
        mUnbinder = ButterKnife.bind(this);

        mCommonDialog = new CommonDialog(this);
        mCommonDialog.setTitle(R.string.unregister_card);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSerarchCardView.setWillNotDraw(false);
        mSerarchCardView.setSearching(true);
        LocalCardBean.getInstance().bindCallcack(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSerarchCardView.setSearching(false);
        LocalCardBean.getInstance().unBindCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        mSerarchCardView = null;
    }

    @Override
    public void onFindCard(final String cardNum) {
        if (mCommonDialog.isShowing()) {
            mCommonDialog.dismiss();
        }
        String newCardNum = String.format(getResources().getString(R.string.card_number),
                cardNum);
        mCommonDialog.setMessage(newCardNum);
        mCommonDialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CardBean cardBean = LocalCardOpe.queryDataByCardNum(getApplication(), cardNum);
                if (cardBean == null) {
                    BaseApplication.showToast(R.string.no_exist_card);
                } else {
                    LocalCardOpe.deleteDataByCardNum(getApplication(), cardNum);
                    BaseApplication.showToast(R.string.unregister_card_success);
                }
                dialog.dismiss();
            }
        });
        mCommonDialog.setNegativeButton(R.string.cancel, null);
        mCommonDialog.setCancelable(true);
        mCommonDialog.show();
        DDLog.i("UnregisterCardActivity.clazz --onFindCard>>>cardNum :" + cardNum);
    }

    @OnClick({R.id.iv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                LocalCardBean.getInstance().unBindCallback();
                mSerarchCardView.setSearching(false);
                this.finish();
                break;
        }
    }
}
