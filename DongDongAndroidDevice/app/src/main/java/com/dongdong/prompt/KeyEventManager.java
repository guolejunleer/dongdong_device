package com.dongdong.prompt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;
import com.dongdong.db.UnlockLogOpe;
import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.phone.ytx.YTXPlayPhone;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.sdk.SocketThreadManager;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.socket.normal.DSPacket;
import com.dongdong.socket.normal.UdpClientSocket;
import com.dongdong.ui.SettingHomeActivity;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.DeviceInfoUtils;
import com.dongdong.utils.SPUtils;
import com.dongdong.utils.TimeZoneUtil;
import com.jr.door.Launcher;
import com.jr.door.R;
import com.jr.gs.JRService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 按键信息处理结果的执行者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class KeyEventManager {

    private Context mContext;
    private KeyEventDialogManager mKeyEventDialogManager;
    private PromptSound mSound;
    private YTXPlayPhone mYTXPlayPhoneManager;

    //房号界面
    private LinearLayout mUnitDevice;
    private LinearLayout mWallDevice;
    private LinearLayout mWallDeviceDongHao;
    private LinearLayout mWallDeviceUnit;
    private LinearLayout mWallDeviceRoomNum;

    private String mKeyboardNumber = "";
    private String mPwd = "";
    private TextView mTvPwd;
    private CommonDialog mPwdDialog;

    private boolean isInputPwdStatus = false;//是否为密码输入状态

    private static KeyEventManager mInstance;

    private KeyEventManager() {
    }

    public static KeyEventManager getInstance() {
        if (mInstance == null) {
            synchronized (KeyEventManager.class) {
                if (mInstance == null)
                    mInstance = new KeyEventManager();
            }
        }
        return mInstance;
    }

    public void initKeyEventManager(Context context, KeyEventDialogManager kdm, YTXPlayPhone phone) {
        this.mContext = context;
        this.mKeyEventDialogManager = kdm;
        this.mYTXPlayPhoneManager = phone;
        mSound = new PromptSound(context);
    }

    public void setUIControlParams(LinearLayout unit, LinearLayout wall, LinearLayout dongHao,
                                   LinearLayout wallUnit, LinearLayout wallRoomNum) {
        this.mUnitDevice = unit;
        this.mWallDevice = wall;
        this.mWallDeviceDongHao = dongHao;
        this.mWallDeviceUnit = wallUnit;
        this.mWallDeviceRoomNum = wallRoomNum;
    }

    public void onUpdateNumberView(final String number) {
        DDLog.i("KeyEventManager.clazz--->>> onUpdateNumberView number:"
                + number + "; mKeyboardNumber:" + mKeyboardNumber);
        if (number.equals("#") || number.equals("*")) {
            mUnitDevice.setVisibility(View.GONE);
            mWallDevice.setVisibility(View.GONE);
            playKeyVoice(number);
            if (number.equals("#")) {// /////////////////////////////////////// 下面是*键的内容操作
                DDLog.i("KeyEventManager.clazz-->> onUpdateNumberView isInputPwdStatus:"
                        + isInputPwdStatus + "; mKeyboardNumber:" + mKeyboardNumber);
                int numberLen = mKeyboardNumber.length();
                if (isInputPwdStatus) {// //////////////////////// 下面是密码框输入操作
                    String pwd = mTvPwd.getText().toString();
                    if (pwd.equals(getManagementPwd()) || pwd.equals(AppConfig.ADMIN_PWD)
                            || pwd.equals(AppConfig.SUPER_ADMIN_PWD)) {//跳转到工程界面
                        goSettingHomeActivity();
                        resetKeyboardEventStatus();
                    } else {// 提示密码错误
                        BaseApplication.showToast(R.string.pwd_error);
                        mSound.pwdError();
                    }
                } // //////////////////////// 上面是密码框输入操作
                else if (numberLen == 0) {// 直接按#键,进入密码输入界面,现去掉密码开门
                    //showPwdDialog();
                    BaseApplication.showToast(R.string.input_room);
                } else if (numberLen == 3 || numberLen == 4 || numberLen == 8) {//优化按3个房号自动补0
                    if (DeviceInfoUtils.getDeviceMode(mContext) == DeviceApplication.DEVICE_MODE_WALL) {// 围墙机
                        if (mKeyboardNumber.equals(AppConfig.WALL_PWD_ENTER_KEY)) { // 进入密码输入界面
                            showPwdDialog();
                        } else if (numberLen == 4 || numberLen == 8) {//围墙机暂时没优化!!!
                            if (!Launcher.mIsALConnected) {//无网
                                BaseApplication.showToast(R.string.check_network);
                                resetKeyboardEventStatus();
                            } else {//查询房号
                                mKeyEventDialogManager.showQueryRoomDialog();
                                mKeyboardNumber = numberLen == 3 ? "0" + mKeyboardNumber
                                        : mKeyboardNumber;
                                DongDongCenter.queryRoomNumber(mKeyboardNumber);
                                Launcher.mHandler.sendEmptyMessageDelayed(
                                        Launcher.UPDATE_DIALOG_WHAT, 2 * 1000);
                                DDLog.i("KeyEventManager.clazz---->># wall " +
                                        "sendEmptyMessageDelayed and queryRoomNumber");
                            }
                        }
                    } else {//单元机
                        if (mKeyboardNumber.equals(AppConfig.UNIT_PWD_ENTER_KEY)) { // 进入密码输入界面
                            showPwdDialog();
                        } else {
                            if (!Launcher.mIsALConnected) {//无网
                                BaseApplication.showToast(R.string.check_network);
                                resetKeyboardEventStatus();
                            } else {//查询房号
                                mKeyEventDialogManager.showQueryRoomDialog();
                                mKeyboardNumber = numberLen == 3 ? "0" + mKeyboardNumber
                                        : mKeyboardNumber;
                                DongDongCenter.queryRoomNumber(mKeyboardNumber);
                                Launcher.mHandler.sendEmptyMessageDelayed(
                                        Launcher.UPDATE_DIALOG_WHAT, 2 * 1000);
                                DDLog.i("KeyEventManager.clazz---->># unit " +
                                        "sendEmptyMessageDelayed and queryRoomNumber");
                            }
                        }
                    }
                } else { // 呼叫失败
                    mSound.inputError();
                    String noRoom = String.format(BaseApplication.resources().
                            getString(R.string.no_room), mKeyboardNumber);
                    BaseApplication.showToast(noRoom);
                    resetKeyboardEventStatus();
                }
            } ///////////////////////////////////////////////以上是#号内容操作
            else {///////////////////////////////////////// 下面是*键的内容操作,取消行动
                DongDongCenter.handUp(1);
                mYTXPlayPhoneManager.hangUp();
                mKeyEventDialogManager.dismissQueryRoomDialog();
                mKeyEventDialogManager.dismissNormalDialog();
                mKeyEventDialogManager.setAdCurrVolume();
                resetKeyboardEventStatus();
                CountTimeRunnable.stopTalkingOrMonitoring();
            }
        } else {///////////////////////////////////////////////// 下面是 0~9数字的操作
            if (isInputPwdStatus) {//去掉密码开门
                mPwd += number;
                mTvPwd.setText(mPwd);
                DDLog.i("KeyEventManager.clazz---->>onUpdateNumberView number:" + number);
            } else {
                if (DeviceInfoUtils.getDeviceMode(mContext) ==
                        DeviceApplication.DEVICE_MODE_WALL) {// 围墙机
                    mWallDevice.setVisibility(View.VISIBLE);
                    boolean maxNum = mKeyboardNumber.length() >= 8;
                    if (maxNum) {
                        playKeyVoice("*");
                    } else {
                        playKeyVoice(number);
                        mKeyboardNumber += number;
                        updateWallDeviceImage(number);
                    }
                } else {// 门口机
                    mUnitDevice.setVisibility(View.VISIBLE);
                    boolean maxNum = mKeyboardNumber.length() >= 4;
                    if (maxNum) {
                        playKeyVoice("*");
                    } else {
                        playKeyVoice(number);
                        mKeyboardNumber += number;
                        updateUnitDeviceImage(number);
                    }
                }
            }
        }
    }

    private long lastTimeMillis;

    /**
     * 显示密码框
     */
    private void showPwdDialog() {
        isInputPwdStatus = true;
        View diaView = View.inflate(mContext, R.layout.password_dialog, null);
        mTvPwd = (TextView) diaView.findViewById(R.id.pwd);
        mTvPwd.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String pwd = mTvPwd.getText().toString().trim();
                int length = pwd.length();
                DDLog.i("KeyEventManager.clazz--->>> pwd editor...... keyCode:" + keyCode
                        + ",pwd:" + pwd + ",length:" + length);
                long currentTimeMillis = System.currentTimeMillis();
                if (Math.abs(currentTimeMillis - lastTimeMillis) > 300
                        && !DeviceApplication.isSystemSettingStatus) {
                    if (keyCode == KeyEvent.KEYCODE_POUND && length > 0) {//#
                        DDLog.i("KeyEventManager.clazz--->>> pwd editor....### (pwd:" + pwd + ").....");
                        if (pwd.equals(getManagementPwd()) || pwd.equals(AppConfig.ADMIN_PWD)
                                || pwd.equals(AppConfig.SUPER_ADMIN_PWD)) {
                            goSettingHomeActivity();
                        } else {
                            DDLog.i("KeyEventManager.clazz--->>> pwd editor....### (pwd:" + pwd + ")");
                            // 提示密码错误
                            BaseApplication.showToast(R.string.pwd_error);
                            mSound.pwdError();
                        }
                        resetKeyboardEventStatus();
                    } else if (keyCode == KeyEvent.KEYCODE_STAR) {//*
                        resetKeyboardEventStatus();
                    }
                }
                lastTimeMillis = currentTimeMillis;
                return false;
            }
        });

        mPwdDialog = new CommonDialog(mContext);
        mPwdDialog.setContent(diaView);
        mPwdDialog.setTitle(R.string.please_input_pwd);
        mPwdDialog.show();
    }

    /**
     * 跳转到工程设置界面
     */
    private void goSettingHomeActivity() {
        Intent intent = new Intent(mContext, SettingHomeActivity.class);
        Bundle bundle = new Bundle();//传输验证码到下一个界面
        if (mTvPwd.getText().toString().equals(AppConfig.SUPER_ADMIN_PWD)) {
            bundle.putString(AppConfig.BUNDLE_SUPER_ADMIN_KEY, AppConfig.SUPER_ADMIN_PWD);
        }
        intent.putExtras(bundle);
        mContext.startActivity(intent);
        //改变状态为系统设置
        DeviceApplication.isSystemSettingStatus = true;
    }

    /**
     * 更新单元机界面
     *
     * @param num 显示的数字
     */
    private void updateUnitDeviceImage(String num) {
        ImageView iv = new ImageView(mContext);
        iv.setImageResource(getMipmapResID(num));
        mUnitDevice.addView(iv);
    }

    /**
     * 更新围墙机界面
     *
     * @param num 显示的数字
     */
    private void updateWallDeviceImage(String num) {
        ImageView iv = new ImageView(mContext);
        iv.setImageResource(getMipmapResID(num));
        int dongHaoCount = mWallDeviceDongHao.getChildCount();
        int unitCount = mWallDeviceUnit.getChildCount();
        int roomNumCount = mWallDeviceRoomNum.getChildCount();
        if (dongHaoCount < 3) {
            if (dongHaoCount == 0) {
                ImageView firstIv = new ImageView(mContext);
                firstIv.setImageResource(R.mipmap.dong_hao);
                mWallDeviceDongHao.addView(firstIv);
            }
            mWallDeviceDongHao.addView(iv);
        } else if (unitCount < 3) {
            if (unitCount == 0) {
                ImageView firstIv = new ImageView(mContext);
                firstIv.setImageResource(R.mipmap.unit);
                mWallDeviceUnit.addView(firstIv);
            }
            mWallDeviceUnit.addView(iv);
        } else if (roomNumCount < 5) {
            if (roomNumCount == 0) {
                ImageView firstIv = new ImageView(mContext);
                firstIv.setImageResource(R.mipmap.room_number);
                mWallDeviceRoomNum.addView(firstIv);
            }
            mWallDeviceRoomNum.addView(iv);
        }
    }

    private int getMipmapResID(String digital) {
        int resId = R.mipmap.number_null;
        switch (digital) {
            case "0":
                resId = R.mipmap.n0;
                break;
            case "1":
                resId = R.mipmap.n1;
                break;
            case "2":
                resId = R.mipmap.n2;
                break;
            case "3":
                resId = R.mipmap.n3;
                break;
            case "4":
                resId = R.mipmap.n4;
                break;
            case "5":
                resId = R.mipmap.n5;
                break;
            case "6":
                resId = R.mipmap.n6;
                break;
            case "7":
                resId = R.mipmap.n7;
                break;
            case "8":
                resId = R.mipmap.n8;
                break;
            case "9":
                resId = R.mipmap.n9;
                break;
        }
        return resId;
    }

    private void playKeyVoice(String key) {
        if (!isInputPwdStatus) {
            mSound.playSound(key);// 播放音频文件
        }
    }

    public static String getNumber(String key) {
        String result;
        switch (key) {
            case "0x08":
                result = "0";
                break;
            case "0x01":
                result = "1";
                break;
            case "0x05":
                result = "2";
                break;
            case "0x09":
                result = "3";
                break;
            case "0x02":
                result = "4";
                break;
            case "0x06":
                result = "5";
                break;
            case "0x0a":
                result = "6";
                break;
            case "0x03":
                result = "7";
                break;
            case "0x07":
                result = "8";
                break;
            case "0x0b":
                result = "9";
                break;
            case "0x04":
                result = "*";
                break;
            case "0x0c":
                result = "#";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    /**
     * 呼叫住户超时
     */
    public void nobodyAnswered() {
        DongDongCenter.handUp(2);
        resetKeyboardEventStatus();
    }

    private String getManagementPwd() {
        String pwd = (String) SPUtils.getParam(mContext,
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_MANAGEMENT_PWD, "");
        if (!TextUtils.isEmpty(pwd)) {
            return pwd;
        }
        return "123456";
    }

    /**
     * 重置界面状态
     */
    public void resetKeyboardEventStatus() {
        if (mWallDeviceDongHao.getChildCount() > 0) {
            mWallDeviceDongHao.removeAllViews();
        }
        if (mWallDeviceUnit.getChildCount() > 0) {
            mWallDeviceUnit.removeAllViews();
        }
        if (mWallDeviceRoomNum.getChildCount() > 0) {
            mWallDeviceRoomNum.removeAllViews();
        }
        if (mUnitDevice.getChildCount() > 0) {
            mUnitDevice.removeAllViews();
        }
        mKeyboardNumber = "";
        mPwd = "";
        isInputPwdStatus = false;
        if (mPwdDialog != null) {
            mPwdDialog.dismiss();
        }
    }


    /**
     * 处理开门记录方法
     *
     * @param result          开门成功或者失败
     * @param unLockType      开门类型
     * @param cardOrPhonedNum 电话号码或者卡号
     */
    public void unlockRequest(final int result, final int unLockType,
                              final String cardOrPhonedNum, final String roomNum) {
        if (result == APlatData.RESULT_SUCCESS) {
            JRService.JRUnlock();
//            DeviceApplication.m_rkctrl.exec_io_cmd(6, 1);//打开继电器控制电磁锁
            mSound.opendoorSucc(true);
            BaseApplication.showToast(R.string.open_door);
            DDLog.i("KeyEventManager.clazz unlockRequest--->>> getUnlockNameByType:" + unLockType
                    + "; cardOrPhonedNum:" + cardOrPhonedNum);
            Date time = TimeZoneUtil.transformTime(new Date(System.currentTimeMillis()),
                    TimeZone.getTimeZone("GMT"), TimeZone.getTimeZone("GMT-08"));
            long unlockTime = time.getTime() / 1000;
            DDLog.i("KeyEventManager.clazz unlockRequest--->>> unlockTime:" + unlockTime);
            //1先获取这条记录保存在本地数据库的id,幸好GreenDao已经做好了
            UnlockLogBean bean = new UnlockLogBean();
            bean.setUnlockType(unLockType);
            bean.setCardOrPhoneNum(cardOrPhonedNum);
            bean.setUnlockTime((int) unlockTime);
            bean.setUpload(AppConfig.UNLOCK_RECORD_NOT_UPLOAD);
            bean.setRoomNum(roomNum);
            //2.将这条数据存到本地数据库
            UnlockLogOpe.insertData(BaseApplication.context(), bean);
            //3.将开门记录信息上报平台
            List<UnlockLogBean> beans = new ArrayList<>();
            beans.add(bean);
            reportUnlockLog2Platform(1, beans);
            deleteUnLockRecordData();
        } else {
            BaseApplication.showToast(R.string.unlock_failed);
            mSound.opendoorSucc(false);
        }
    }

    /**
     * 上传开门记录给平台
     *
     * @param dataType 上传数据类型:本地数据、时时数据
     * @param unlocks  上传数据集合
     */
    private void reportUnlockLog2Platform(final int dataType, final List<UnlockLogBean> unlocks) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.unLockTypeResult(0, dataType, unlocks);
                    if (callPkt == null) {
                        return;
                    }
                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "unlockRequest");
    }

    /**
     * 历史开门记录上传到平台
     */
    public void getUnlockLog() {
        List<UnlockLogBean> unlockLogBeans = UnlockLogOpe.queryDataByUnlockState(
                mContext.getApplicationContext(), AppConfig.UNLOCK_RECORD_NOT_UPLOAD);
        if (unlockLogBeans == null) {
            return;
        }
        int size = unlockLogBeans.size();
        DDLog.i("KeyEventManager.clazz getUnlockLog--->>> size:" + size);
        if (size > 0) {
            int switchValue = size / AppConfig.MAX_UPLOAD_UNLOCK_COUNT;
            DDLog.i("KeyEventManager.clazz getUnlockLog--->>> switchValue:" + switchValue);
            if (switchValue < 1) {
                //如果本地保存数据小于上传数量最大值，那么直接将所有数据上传
                reportUnlockLog2Platform(0, unlockLogBeans);
            } else {
                //如果本地保存数据大于上传数量最大值，那么一次就上传规定的最大条数
//                int count = size;
//                int autoCount = 0;
//                while ((count / AppConfig.MAX_UPLOAD_UNLOCK_COUNT) >= 1) {
//                    List<UnlockLogBean> tempSq = unlockLogBeans.subList(
//                            autoCount * AppConfig.MAX_UPLOAD_UNLOCK_COUNT,
//                            (autoCount + 1) * AppConfig.MAX_UPLOAD_UNLOCK_COUNT);
//                    DDLog.i("KeyEventManager.clazz getUnlockLog--->>> count:" + count
//                            + ";autoCount:" + autoCount + ";tempSq.size:" + tempSq.size());
//                    reportUnlockLog2Platform(0, tempSq);
//                    count -= AppConfig.MAX_UPLOAD_UNLOCK_COUNT;
//                    autoCount++;
//                }
                List<UnlockLogBean> temps = unlockLogBeans.subList(0,
                        AppConfig.MAX_UPLOAD_UNLOCK_COUNT);
                reportUnlockLog2Platform(0, temps);
            }
        } else {
            reportUnlockLog2Platform(0, null);
        }
    }

    private void deleteUnLockRecordData() {
        List<UnlockLogBean> beans = UnlockLogOpe.queryAll(BaseApplication.context());
        if (beans == null) {
            return;
        }
        DDLog.i("DongDongTransferCenter.clazz--->>deleteUploadData() queryAll.size:" + beans.size());
        int count = beans.size() - AppConfig.UNLOCK_RECORD_COUNT;
        if (count > 0) {
            List<Long> unLockIndex = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                long unLockId = beans.get(i).getId();
                unLockIndex.add(unLockId);
            }
            UnlockLogOpe.deleteData(BaseApplication.context(), unLockIndex);
        }
    }

}
