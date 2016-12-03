package com.dongdong.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dongdong.base.BaseApplication;
import com.dongdong.bean.LocalCardBean;
import com.dongdong.db.LocalCardOpe;
import com.dongdong.db.RoomCardOpe;
import com.dongdong.db.entry.CardBean;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.interf.CardNumCallback;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.dongdong.widget.SearchDevicesView;
import com.jr.door.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 校验本地所有卡号信息界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class VerifyCardActivity extends Activity implements CardNumCallback {

    private Unbinder mUnbinder;
    @BindView(R.id.search_device_view)
    SearchDevicesView mSerarchCardView;
    private String mCardType;

    private CommonDialog mCommonDialog;
    private TextView mTvCardType;
    private TextView mTvCardNum;
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.card_react_activity_main);
        mCommonDialog = new CommonDialog(this);
        mView = LayoutInflater.from(VerifyCardActivity.this).inflate(R.layout.verifycard, null);
        mTvCardType = (TextView) mView.findViewById(R.id.tvcardType);
        mTvCardNum = (TextView) mView.findViewById(R.id.tv_card_num);
        mUnbinder = ButterKnife.bind(this);
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

    @OnClick({R.id.iv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                mSerarchCardView.setSearching(false);
                this.finish();
                break;
        }
    }

    @Override
    public void onFindCard(final String cardNum) {
        if (mCommonDialog.isShowing()) {
            mCommonDialog.dismiss();
        }
        mTvCardNum.setText(cardNum);
        CardBean cardBean = LocalCardOpe.queryDataByCardNum(BaseApplication.context(), cardNum);
        List<RoomCardBean> roomCardBean = RoomCardOpe.queryDataByCardNum(
                BaseApplication.context(), cardNum);
        if (cardBean != null && roomCardBean == null) {
            mCardType = getResources().getString(R.string.native_card);
        } else if (cardBean == null && roomCardBean != null) {
            mCardType = getResources().getString(R.string.platform_card);
        } else if (cardBean != null && roomCardBean != null) {
            mCardType = getResources().getString(R.string.native_platform_card);
        } else {
            //BaseApplication.showToast(R.string.no_exist_card);
            mCardType = getResources().getString(R.string.no_exist_card);
        }
        mTvCardType.setText(mCardType);
        mCommonDialog.setContent(mView);
        mCommonDialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mCommonDialog.setNegativeButton(R.string.cancel, null);
        mCommonDialog.setCancelable(true);
        mCommonDialog.show();
        DDLog.i("VerifyCardActivity.clazz --onFindCard>>>cardNum :" + cardNum);
    }
}
