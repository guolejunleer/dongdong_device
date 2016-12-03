package com.dongdong.ui;

import com.dongdong.base.BaseApplication;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.SPUtils;
import com.jr.door.R;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 用户设置主界面，目前包括声音、亮度
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class UserSettingActivity extends Activity implements OnSeekBarChangeListener {

    private Unbinder mUnbinder;
    @BindView(R.id.device_audio_bar)
    SeekBar mSeekBarAudio;
    @BindView(R.id.device_ad_bar)
    SeekBar mSeekBarAd;
    @BindView(R.id.device_light_bar)
    SeekBar mSeekBarLight;
    @BindView(R.id.iv_auto)
    ImageView mIvAuto;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.user_setting_activity);
        mUnbinder = ButterKnife.bind(this);
        //1. 音量控制,初始化定义
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int curSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSeekBarAudio.setProgress(curSystemVolume);

        //2.广告声音
        float curAdVolume = (float) SPUtils.getParam(BaseApplication.context(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_AD_VOLUME, 1.0f);
        mSeekBarAd.setProgress((int) (curAdVolume * 100));

        //3.调节亮度
        int screenMode = 0;
        try {
            screenMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (screenMode == 0) {
            mIvAuto.setImageResource(R.mipmap.select);
        } else {
            mIvAuto.setImageResource(R.mipmap.selected);
        }
        int screenBrightness = 0; // 0--255获得当前亮度
        try {
            screenBrightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        mSeekBarLight.setProgress(screenBrightness);

        mSeekBarAudio.setOnSeekBarChangeListener(this);
        mSeekBarAd.setOnSeekBarChangeListener(this);
        mSeekBarLight.setOnSeekBarChangeListener(this);

        DDLog.i("UserSettingActivity.clazz--->>>onCreate curSystemVolume:"
                + curSystemVolume + ",curAdVolume:" + curAdVolume);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick({R.id.iv_back, R.id.iv_auto})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.iv_auto:
                try {
                    if (Settings.System.getInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) {
                        mIvAuto.setImageResource(R.mipmap.select);
                        Settings.System.putInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
                    } else {
                        mIvAuto.setImageResource(R.mipmap.selected);
                        Settings.System.putInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
                    }
                    mSeekBarLight.setProgress(Settings.System.getInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS));
                } catch (SettingNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mSeekBarAudio) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mSeekBarAudio.getProgress(), 0); // tempVolume:音量绝对值
        } else if (seekBar == mSeekBarAd) {
            float progress = mSeekBarAd.getProgress();
            float volume = progress / 100.0f;
            DDLog.i("UserSettingActivity.clazz--->>> onStopTrackingTouch volume:"
                    + volume);
            SPUtils.setParam(BaseApplication.context(),
                    SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_AD_VOLUME, volume);
        } else if (seekBar == mSeekBarLight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, mSeekBarLight.getProgress());
        }
    }
}
