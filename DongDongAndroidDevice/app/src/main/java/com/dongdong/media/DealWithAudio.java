package com.dongdong.media;

import android.annotation.SuppressLint;
import android.media.audiofx.AcousticEchoCanceler;

/**
 * 回声消除类
 *
 * @author leer
 *
 */
@SuppressLint("NewApi")
public class DealWithAudio {

    private AcousticEchoCanceler mCanceler;

    public static boolean isDeviceSupport() {
        return AcousticEchoCanceler.isAvailable();
    }

    public boolean initAEC(int audioSession) {
        if (mCanceler != null) {
            return false;
        }
        mCanceler = AcousticEchoCanceler.create(audioSession);
        if (mCanceler == null) {
            return false;
        }
        mCanceler.setEnabled(true);
        return mCanceler.getEnabled();
    }

    public boolean setAECEnabled(boolean enable) {
        if (null == mCanceler) {
            return false;
        }
        mCanceler.setEnabled(enable);
        return mCanceler.getEnabled();
    }

    public boolean release() {
        if (null == mCanceler) {
            return false;
        }
        // if (mCanceler.getEnabled()) {
        // mCanceler.setEnabled(false);
        // }
        mCanceler.release();
        return true;
    }

    public void startTheEchoCancellation(int id) {
        if (!isDeviceSupport()) {
            return;
        }
        boolean initAEC = initAEC(id);
        if (!initAEC) {
            return;
        }
        setAECEnabled(true);
    }
}
