package com.dongdong.prompt;

import com.jr.door.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

/**
 * 按键信息的播放状态声音管理者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class PromptSound {
    static SoundPool pool;
    private int zero, one, two, three, four, five, six, seven, eight, nine,
            pro, open_door_ok, open_door_failed, pwdError, inputError;
    private static int callfail;
    private static int pleasecall;
    private static int talkOver;
    private static boolean onLoadComplete = false;

    public PromptSound(Context context) {
        // 指定声音池的最大音频流数目为10，声音品质为5
        pool = new SoundPool(30, AudioManager.STREAM_SYSTEM, 0);
        // 载入音频流，返回在池中的id
        zero = pool.load(context, R.raw.zero, 0);
        one = pool.load(context, R.raw.one, 0);
        two = pool.load(context, R.raw.two, 0);
        three = pool.load(context, R.raw.three, 0);
        four = pool.load(context, R.raw.four, 0);
        five = pool.load(context, R.raw.five, 0);
        six = pool.load(context, R.raw.six, 0);
        seven = pool.load(context, R.raw.seven, 0);
        eight = pool.load(context, R.raw.eight, 0);
        nine = pool.load(context, R.raw.nine, 0);
        pro = pool.load(context, R.raw.sound1, 0);
        open_door_ok = pool.load(context, R.raw.opendoorok, 0);
        open_door_failed = pool.load(context, R.raw.opendoorko, 0);
        callfail = pool.load(context, R.raw.callfail, 0);
        pleasecall = pool.load(context, R.raw.pleasecall, 0);
        talkOver = pool.load(context, R.raw.talk_over, 0);
        pwdError = pool.load(context, R.raw.passerror, 0);
        inputError = pool.load(context, R.raw.inputerror, 0);

        pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
                onLoadComplete = true;
            }
        });
    }

    public void playSound(String number) {
        if (!onLoadComplete) {
            return;
        }
        switch (number) {
            case "#":
            case "*":
                pool.play(pro, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "0":
                pool.play(zero, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "1":
                pool.play(one, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "2":
                pool.play(two, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "3":
                pool.play(three, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "4":
                pool.play(four, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "5":
                pool.play(five, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "6":
                pool.play(six, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "7":
                pool.play(seven, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "8":
                pool.play(eight, 0.3f, 0.3f, 0, 0, 1);
                break;
            case "9":
                pool.play(nine, 0.3f, 0.3f, 0, 0, 1);
                break;
        }
    }

    public void opendoorSucc(boolean yes) {
        if (!onLoadComplete) {
            return;
        }
        if (yes) {
            pool.play(open_door_ok, 0.3f, 0.3f, 0, 0, 1);
        } else {
            pool.play(open_door_failed, 0.3f, 0.3f, 0, 0, 1);
        }
    }

    public static void callResult(boolean yes) {
        if (!onLoadComplete) {
            return;
        }
        if (yes) {
            pool.play(pleasecall, 0.3f, 0.3f, 0, 0, 1);
        } else {
            pool.play(callfail, 0.3f, 0.3f, 0, 0, 1);
        }
    }

    public static void talkOver() {
        pool.play(talkOver, 0.3f, 0.3f, 0, 0, 1);
    }

    public void pwdError() {
        pool.play(pwdError, 0.3f, 0.3f, 0, 0, 1);
    }

    public void inputError() {
        pool.play(inputError, 0.3f, 0.3f, 0, 0, 1);
    }

}
