package com.dongdong.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.dongdong.AppConfig;
import com.dongdong.base.BaseApplication;
import com.dongdong.DeviceApplication;
import com.dongdong.DeviceApplication.OnKeyboardEventsChangeListener;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.DeviceInfoUtils;
import com.dongdong.utils.SDCardUtils;
import com.dongdong.utils.SPUtils;
import com.jr.door.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 系统设置主界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class SettingHomeActivity extends Activity implements OnKeyboardEventsChangeListener,
        OnClickListener {

    private Unbinder mUnbinder;
    @BindView(R.id.iv_deviceinfo_setting)
    ImageView mSystemInfo;
    @BindView(R.id.iv_back)
    ImageView mBack;
    @BindView(R.id.iv_net_setting)
    ImageView mWifiSetting;
    @BindView(R.id.iv_card_setting)
    ImageView mDoorSetting;
    @BindView(R.id.iv_pwd_etting)
    ImageView mPwdSetting;
    @BindView(R.id.iv_devicemode_setting)
    ImageView mSysSetting;
    @BindView(R.id.iv_user_setting)
    ImageView mUserSetting;

    private String mSuperAdminPwd;//工程密码

    private static List<OnKeyEventChangeListener> mListeners = new ArrayList();
    private CommonDialog mPwdSettingsDialog;
    private CommonDialog mDeviceModeSetDialog;

    public interface OnKeyEventChangeListener {
        void onKeyEventChange(String num);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.setting_activity);
        // 通过注解绑定控件
        mUnbinder = ButterKnife.bind(this);
        // //////////////////////////得到工程密码类型
        Bundle bundle = this.getIntent().getExtras();
        mSuperAdminPwd = bundle.getString(AppConfig.BUNDLE_SUPER_ADMIN_KEY, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSuperAdminPwd.equals(AppConfig.SUPER_ADMIN_PWD)) {
            mSysSetting.setVisibility(View.VISIBLE);
            ((DeviceApplication) getApplication()).addOnKeyboardEventsChangeListener(this);
        } else {
            mSysSetting.setVisibility(View.GONE);
        }
        DDLog.i("SettingsActivity.class onResume>>>>>>>>>add>>>>>>>>>>>>>");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSuperAdminPwd.equals(AppConfig.SUPER_ADMIN_PWD)) {
            ((DeviceApplication) getApplication()).removeOnKeyboardEventsChangeListener(this);
        }
        DDLog.i("SettingsActivity.class onPause>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    public static void recycleImageView(View view) {
        if (view == null) return;
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
                if (bmp != null && !bmp.isRecycled()) {
                    ((ImageView) view).setImageBitmap(null);
                    bmp.recycle();
                    bmp = null;
                }
            }
        }
    }

    @OnClick({R.id.iv_deviceinfo_setting, R.id.iv_net_setting, R.id.iv_user_setting, R.id.iv_card_setting,
            R.id.iv_pwd_etting, R.id.iv_devicemode_setting, R.id.iv_back})
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_deviceinfo_setting://跳转设备信息界面
                this.startActivity(new Intent(this, DeviceInfoActivity.class));
                break;
            case R.id.iv_net_setting://跳转选择设置网卡界面
                netSettings();
                break;
            case R.id.iv_user_setting://跳转用户设置界面
                this.startActivity(new Intent(this, UserSettingActivity.class));
                break;
            case R.id.iv_card_setting://跳转卡号设置界面
                this.startActivity(new Intent(this, CardManagementActivity.class));
                break;
            case R.id.iv_pwd_etting://跳转密码设置界面
                pwdSettings();
                break;
            case R.id.iv_devicemode_setting://跳转设备模式设置界面
                deviceModeSetting();
                break;
            case R.id.iv_back:
                this.finish();
                break;
            default:
                break;
        }
    }

    private void netSettings() {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setTitle(R.string.wifi_set);
        commonDialog.setMessage(R.string.choose_network);
        commonDialog.setPositiveButton(R.string.net_card_2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SettingHomeActivity.this, WifiSettingActivity.class);
                SettingHomeActivity.this.startActivity(intent);
                dialog.dismiss();
            }
        });
        commonDialog.setNegativeButton(R.string.net_card_1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                SettingHomeActivity.this.startActivity(intent);
                dialog.dismiss();
            }
        });
        commonDialog.setCancelable(true);
        commonDialog.show();
    }

    private void pwdSettings() {
        View diaView = View.inflate(this, R.layout.pwd_setting, null);
        final EditText etFirstPwd = (EditText) diaView.findViewById(R.id.pwd);
        final EditText etSecondPwd = (EditText) diaView.findViewById(R.id.againpwd);
        final PwdKeyEventRecieve pwdKeyEventRecieve = new PwdKeyEventRecieve(etFirstPwd, etSecondPwd);
        //添加按键注册接收者
        mListeners.add(pwdKeyEventRecieve);

        mPwdSettingsDialog = new CommonDialog(this);
        mPwdSettingsDialog.setContent(diaView);
        mPwdSettingsDialog.setTitle(R.string.pwd_settings);
        mPwdSettingsDialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pwd1 = etFirstPwd.getText().toString();
                String pwd2 = etSecondPwd.getText().toString();
                if (pwd1.length() != 6) {
                    BaseApplication.showToast(R.string.pwd_max_num);
                    return;
                }
                if (pwd1.equals(pwd2)) {
                    SPUtils.setParam(getApplicationContext(), SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                            SPUtils.SP_KEY_MANAGEMENT_PWD, etFirstPwd);
                    dialog.dismiss();
                } else {
                    BaseApplication.showToast(R.string.pwd_not_correct_twice);
                    return;
                }
                dialog.dismiss();
            }
        });
        mPwdSettingsDialog.setNegativeButton(R.string.cancel, null);
        mPwdSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //取消按键注册接收者
                mListeners.remove(pwdKeyEventRecieve);
            }
        });
        mPwdSettingsDialog.setCancelable(true);
        mPwdSettingsDialog.show();
    }

    private void deviceModeSetting() {
        View diaView = View.inflate(this, R.layout.model_setting, null);
        final CheckBox cbUnitMode = (CheckBox) diaView.findViewById(R.id.cb_device_unit_mode);
        final CheckBox cbWallMode = (CheckBox) diaView.findViewById(R.id.cb_device_wall_mode);
        final EditText etDevcieId = (EditText) diaView.findViewById(R.id.id_device);
        cbUnitMode.setChecked(DeviceInfoUtils.getDeviceMode(getApplicationContext())
                == DeviceApplication.DEVICE_MODE_UNIT);
        cbWallMode.setChecked(DeviceInfoUtils.getDeviceMode(getApplicationContext())
                == DeviceApplication.DEVICE_MODE_WALL);
        String deviceId = DeviceInfoUtils.getDeviceID(getApplicationContext());
        etDevcieId.setText(deviceId);
        etDevcieId.setSelection(deviceId.length());
        final DeviceModeKeyEventRecieve ameRecieve = new DeviceModeKeyEventRecieve(etDevcieId, cbUnitMode);
        //添加按键注册接收者
        mListeners.add(ameRecieve);
        cbUnitMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = cbUnitMode.isChecked();
                cbUnitMode.setChecked(checked);
                cbWallMode.setChecked(!checked);
            }
        });
        cbWallMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = cbWallMode.isChecked();
                cbWallMode.setChecked(checked);
                cbUnitMode.setChecked(!checked);
            }
        });

        mDeviceModeSetDialog = new CommonDialog(this);
        mDeviceModeSetDialog.setContent(diaView);
        mDeviceModeSetDialog.setTitle(R.string.pwd_settings);
        mDeviceModeSetDialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(etDevcieId.getText().toString())) {
                    BaseApplication.showToast(R.string.no_device_id);
                    return;
                }
                // 保存设备ID
                DeviceInfoUtils.setDeviceID(getApplicationContext(), etDevcieId.getText().toString());

                //保存设备模式
                boolean unitChecked = cbUnitMode.isChecked();
                int deviceMode = unitChecked ? DeviceApplication.DEVICE_MODE_UNIT
                        : DeviceApplication.DEVICE_MODE_WALL;
                DeviceInfoUtils.setDeviceMode(getApplicationContext(), deviceMode);

                if (SDCardUtils.isSDCardEnable()) {
                    String deviceData = etDevcieId.getText().toString() + "#" + deviceMode;
                    new WriteDataTask().execute(AppConfig.SDCARD_FILE,
                            AppConfig.SDCARD_FILE_NAME, deviceData);
                }
                dialog.dismiss();
            }
        });
        mDeviceModeSetDialog.setNegativeButton(R.string.cancel, null);
        mDeviceModeSetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {  //取消按键注册接收者
                mListeners.remove(ameRecieve);
            }
        });
        mDeviceModeSetDialog.setCancelable(true);
        mDeviceModeSetDialog.show();
    }

    private class WriteDataTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            File file;
            try {
                file = new File(SDCardUtils.createDirOnSDCard(params[0]), params[1]);
                SDCardUtils.writeData2SDCard(file, params[2].getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                DDLog.e("SettingsActivity.class WriteDataTask had exception =" + e);
            }
            return null;
        }
    }

    @Override
    public void onLocalCardUnlock(int unlockType, String cardNum) {
    }

    @Override
    public void onSendCardUnlock(String cardNum) {
    }

    @Override
    public void onUpdateNumberView(String number) {
        int size = mListeners.size();
        DDLog.i("SettingsActivity.class onChangKeyValue>>number:" + number
                + ";listener size:" + size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mListeners.get(i).onKeyEventChange(number);
            }
        }
    }

    private class PwdKeyEventRecieve implements OnKeyEventChangeListener {
        EditText etFirst;
        EditText etSecond;

        PwdKeyEventRecieve(EditText etFirst, EditText etSecond) {
            this.etFirst = etFirst;
            this.etSecond = etSecond;
        }

        @Override
        public void onKeyEventChange(String number) {
            String pwdFirst = etFirst.getText().toString();
            String pwdSecond = etSecond.getText().toString();
            int pwdFirstLen = pwdFirst.length();
            int pwdSecondLen = pwdSecond.length();
            boolean isFirstEtInput = pwdFirstLen < 6;
            DDLog.i("pwd onKeyEventChange-->>> pwdFirstLen:" + pwdFirstLen
                    + "; pwdSecondLen " + pwdSecondLen);
            if ("*".equals(number)) {
                if (pwdFirstLen <= 0) {
                    BaseApplication.showToast(R.string.edit_view_tip);
                    return;
                }
                //这里之所以用到第二个密码框的数字长度是为了按*键删除跳到第一个密码框
                if (isFirstEtInput || pwdSecondLen <= 0) {
                    pwdFirst = pwdFirst.substring(0, pwdFirstLen - 1);
                    etFirst.setText(pwdFirst);
                } else {
                    pwdSecond = pwdSecond.substring(0, pwdSecondLen <= 0 ? 0 : pwdSecondLen - 1);
                    etSecond.setText(pwdSecond);
                }
            } else if ("#".equals(number)) {
                if (pwdFirst.length() != 6) {
                    BaseApplication.showToast(R.string.pwd_max_num);
                    return;
                }
                if (pwdFirst.equals(pwdSecond)) {
                    SPUtils.setParam(getApplicationContext(), SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                            SPUtils.SP_KEY_MANAGEMENT_PWD, pwdSecond);
                    mPwdSettingsDialog.dismiss();
                } else {
                    BaseApplication.showToast(R.string.pwd_not_correct_twice);
                    return;
                }
            } else {
                if (isFirstEtInput) {
                    pwdFirst += number;
                    etFirst.setText(pwdFirst);
                } else {
                    pwdSecond += number;
                    etSecond.setText(pwdSecond);
                }
            }
            if (isFirstEtInput) {
                etFirst.setSelection(etFirst.length());
                etFirst.setFocusable(true);
                etFirst.requestFocus();
            } else {
                etSecond.setSelection(etSecond.length());
                etSecond.setFocusable(true);
                etSecond.requestFocus();
            }
        }
    }

    private class DeviceModeKeyEventRecieve implements OnKeyEventChangeListener {
        EditText et;
        CheckBox cbUnitMode;

        DeviceModeKeyEventRecieve(EditText et, CheckBox cbUnitMode) {
            this.et = et;
            this.cbUnitMode = cbUnitMode;
        }

        @Override
        public void onKeyEventChange(String number) {
            String deviceNum = et.getText().toString();
            if ("*".equals(number)) {
                if (deviceNum.length() == 0) {
                    BaseApplication.showToast(R.string.edit_view_tip);
                    return;
                }
                deviceNum = deviceNum.substring(0, deviceNum.length() - 1);
                et.setText(deviceNum);
            } else if ("#".equals(number)) {
                if (TextUtils.isEmpty(et.getText().toString())) {
                    BaseApplication.showToast(R.string.no_device_id);
                    return;
                }
                // /////保存id
                SPUtils.setParam(getApplicationContext(),
                        SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_DEVICE_ID,
                        et.getText().toString());

                //设置参数
                boolean unitChecked = cbUnitMode.isChecked();
                int deviceMode = unitChecked ? DeviceApplication.DEVICE_MODE_UNIT
                        : DeviceApplication.DEVICE_MODE_WALL;
                DeviceInfoUtils.setDeviceMode(getApplicationContext(), deviceMode);

                if (SDCardUtils.isSDCardEnable()) {
                    String deviceData = et.getText().toString() + "#" + deviceMode;
                    new WriteDataTask().execute(AppConfig.SDCARD_FILE,
                            AppConfig.SDCARD_FILE_NAME, deviceData);
                }
                mDeviceModeSetDialog.dismiss();
            } else {
                deviceNum += number;
                et.setText(deviceNum);
            }
            et.setSelection(et.length());
        }
    }
}
