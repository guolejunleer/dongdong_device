package com.dongdong.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongdong.DeviceApplication;
import com.dongdong.interf.LauncherAndBackendSignalCallback;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.socket.normal.InfoNetParam;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.jr.door.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wifi设置主界面
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class WifiSettingActivity extends Activity implements
        LauncherAndBackendSignalCallback, OnClickListener {

    private ImageView mBack;
    private EditText mNetType, mEthIP, mEthMask, mDefaultGW, mPrimaryDNS,
            mSecondaryDNS, mMacAddress;
    private TextView mEthAddrType, mDNSAddrType;
    private Button mSure, mCancel;
    private InfoNetParam infoNetParam = new InfoNetParam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.wifi_setting_activity);

        mBack = (ImageView) findViewById(R.id.back);
        mNetType = (EditText) findViewById(R.id.nettype);
        mEthAddrType = (TextView) findViewById(R.id.netaddrtype);
        mEthIP = (EditText) findViewById(R.id.ethip);
        mEthMask = (EditText) findViewById(R.id.ethmask);
        mDefaultGW = (EditText) findViewById(R.id.defaultgw);
        mDNSAddrType = (TextView) findViewById(R.id.dnsaddrtype);
        mPrimaryDNS = (EditText) findViewById(R.id.primarydns);
        mSecondaryDNS = (EditText) findViewById(R.id.secondarydns);
        mMacAddress = (EditText) findViewById(R.id.macaddress);
        mSure = (Button) findViewById(R.id.ok);
        mCancel = (Button) findViewById(R.id.cancel);

        mSure.setOnClickListener(mSureButtonClick);
        mCancel.setOnClickListener(this);
        mEthAddrType.setOnClickListener(mEtchAddrTypeClick);
        mDNSAddrType.setOnClickListener(mDNSAddrTypeClick);
        mBack.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        DongDongCenter.setWifiSettingsActivity(this);
        DDLog.d("WifiSettingActivity.class onResume--->>> DongDongCenter.mLAndBSignalCallback："
                + DongDongCenter.mLAndBSignalCallback);
        DongDongCenter.getNetRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DongDongCenter.mLAndBSignalCallback = null;
        DDLog.d("WifiSettingActivity.class onPause--->>> DongDongCenter.mLAndBSignalCallback："
                + DongDongCenter.mLAndBSignalCallback);
    }

    /**
     * 把IP地址转化为int
     *
     * @param ipAddr
     * @return int
     */
    public static int ipToInt(String ipAddr) {
        byte[] ret = new byte[4];
        try {
            String[] ipArr = ipAddr.split("\\.");
            ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
            ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
            ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
            ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);

            int addr = ret[3] & 0xFF;
            addr |= ((ret[2] << 8) & 0xFF00);
            addr |= ((ret[1] << 16) & 0xFF0000);
            addr |= ((ret[0] << 24) & 0xFF000000);
            return addr;
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }

    }

    public String getType(int type) {
        if (type == 1) {
            return "动态获取";
        } else if (type == 2) {
            return "静态配置";
        }
        return "未知";
    }

    @Override
    public int onGetNetResult(int cmdflag, InfoNetParam netparam) {
        mNetType.setText(netparam.getNettype());
        mEthAddrType.setText(getType(netparam.getEthaddrtype()));
        mEthIP.setText(netparam.getEthip());
        mEthMask.setText(netparam.getEthmask());
        mDefaultGW.setText(netparam.getDefaultgw());
        mDNSAddrType.setText(getType(netparam.getDnsaddrtype()));
        mPrimaryDNS.setText(netparam.getPrimarydns());
        mSecondaryDNS.setText(netparam.getSecondarydns());
        mMacAddress.setText(netparam.getMacaddress());
        // /////////////////
        infoNetParam.setEthaddrtype((byte) netparam.getEthaddrtype());
        infoNetParam.setDnsaddrtype((byte) netparam.getDnsaddrtype());

        DDLog.i("WifiSettingsActity.clazz-->>>onGetNetResult comming netparam:"
                + netparam.getNettype()
                + ";"
                + getType(netparam.getEthaddrtype())
                + "; "
                + netparam.getEthip() + "; mNetType:" + mNetType);
        return 0;
    }

    @Override
    public int onSetNetResult(int cmdflag, int result) {
        if (result == 0) {
            Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    /**
     * 验证IP地址
     *
     * @param str 待验证的字符串
     * @return 如果是符合格式的字符串, 返回 <b>true </b>,否则为 <b>false </b>
     * "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
     * ;
     */
    public static boolean isIP(String str) {
        String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num
                + "$";
        return match(regex, str);
    }

    private static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    private OnClickListener mSureButtonClick = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            String ip = mEthIP.getText().toString();// ip
            if (isIP(ip)) {
                infoNetParam.setEthip(ipToInt(ip));
            } else {
                Toast.makeText(WifiSettingActivity.this, "ip格式不正确",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String mask = mEthMask.getText().toString();
            if (isIP(mask)) {
                infoNetParam.setEthmask(ipToInt(mask));
            } else {
                Toast.makeText(WifiSettingActivity.this, "子网掩码格式不正确",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // ///////////
            String gw = mDefaultGW.getText().toString();// 缺省网关
            if (isIP(gw)) {
                infoNetParam.setDefaultgw(ipToInt(gw));
            } else {
                Toast.makeText(WifiSettingActivity.this, "缺省网关格式不正确",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // //////// primarydnsk NDS
            String dns = mPrimaryDNS.getText().toString();
            if (isIP(dns)) {
                infoNetParam.setPrimarydns(ipToInt(dns));
            } else {
                Toast.makeText(WifiSettingActivity.this, "DNS格式不正确",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // //////// 备用NDS
            String seconddns = mSecondaryDNS.getText().toString();
            if (isIP(seconddns)) {
                infoNetParam.setSecondarydns(ipToInt(seconddns));
            } else {
                Toast.makeText(WifiSettingActivity.this, "备用DNS格式不正确",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String mac = mMacAddress.getText().toString();
            infoNetParam.setMacaddress(mac);
            DongDongCenter.setNetRequest(0, infoNetParam);

        }
    };

    private OnClickListener mEtchAddrTypeClick = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            final CommonDialog dialog = new CommonDialog(WifiSettingActivity.this);
            dialog.setPositiveButton(R.string.dynamic_get, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    infoNetParam.setEthaddrtype((byte) 1);
                    mEthAddrType.setText(R.string.dynamic_get);
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton(R.string.static_get, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    infoNetParam.setEthaddrtype((byte) 2);
                    mEthAddrType.setText(R.string.static_get);
                    dialog.dismiss();
                }
            });
        }
    };

    private OnClickListener mDNSAddrTypeClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final CommonDialog dialog = new CommonDialog(WifiSettingActivity.this);
            dialog.setPositiveButton(R.string.dynamic_get, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    infoNetParam.setEthaddrtype((byte) 1);
                    mDNSAddrType.setText(R.string.dynamic_get);
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton(R.string.static_get, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    infoNetParam.setEthaddrtype((byte) 2);
                    mDNSAddrType.setText(R.string.static_get);
                    dialog.dismiss();
                }
            });
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel:
            case R.id.back:
                finish();
                break;
        }
    }
}
