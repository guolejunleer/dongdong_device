package com.dongdong.ui;

import com.dongdong.DeviceApplication;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.DeviceInfoUtils;
import com.dongdong.utils.NetUtils;
import com.dongdong.utils.SPUtils;
import com.dongdong.utils.UpdateManager;
import com.jr.door.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 系统设备信息界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class DeviceInfoActivity extends Activity {

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.updata)
    Button mUpdate;
    @BindView(R.id.deviceID)
    TextView mDeviceID;
    @BindView(R.id.sysVer)
    TextView mSysoVersion;
    @BindView(R.id.driveVer)
    TextView mDriveVer;
    @BindView(R.id.netStatus)
    TextView mNetStatus;
    @BindView(R.id.serverStatus)
    TextView mServerStatus;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.device_info_activity);
        mUnbinder = ButterKnife.bind(this);
        mDeviceID.setText(String.format(getResources().getString(R.string.device_id),
                getDeviceID()));
        String systemVerState = DDLog.isDebug ? "(Debug)" : "(Release)";
        String systemVer = String.format(getResources().getString(R.string.system_version),
                DeviceInfoUtils.getVersionName(this)) + systemVerState;
        mSysoVersion.setText(systemVer);
        mDriveVer.setText(String.format(getResources().getString(R.string.kernel_version),
                DeviceApplication.mVersion));
        mNetStatus.setText(String.format(getResources().getString(R.string.double_net_state),
                getNormal()));
        mServerStatus.setText(String.format(getResources().getString(R.string.platform_conn_state),
                getServerStatus(DeviceApplication.mRegStatus)));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick({R.id.back, R.id.updata})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                DeviceInfoActivity.this.finish();
                break;
            case R.id.updata:
                if (!NetUtils.isConnected(DeviceInfoActivity.this)) {
                    NetUtils.withoutNetworkAlert(DeviceInfoActivity.this);
                    return;
                }
                new UpdateManager(DeviceInfoActivity.this, true).checkUpdate();
                break;
        }
    }

    public String getDeviceID() {// 得到设备ID
        String devieNum = (String) SPUtils.getParam(getApplicationContext(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_DEVICE_ID, "0");
        return devieNum;
    }

    public String getServerStatus(int status) {
        String result;
        switch (status) {
            case 0:
                result = "准备注册";
                break;
            case 1:
                result = "注册成功";
                break;
            case 2:
                result = "单机模式";
                break;
            case 3:
                result = "未发现平台";
                break;
            case 4:
                result = "平台地址解析失败";
                break;
            case 5:
                result = "正在注册";
                break;
            case 6:
                result = "平台连接失败";
                break;
            case 7:
                result = "平台连接断线";
                break;
            case 8:
                result = "平台未授权";
                break;
            default:
                result = "未知错误";
                break;
        }
        return result;
    }

    public String getNormal() {
        if (DeviceApplication.mNetStateCount == 0) {
            mServerStatus.setVisibility(View.GONE);
            return "异常";
        }
        mServerStatus.setVisibility(View.VISIBLE);
        return "正常";
    }
}
