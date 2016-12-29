package com.dongdong.interf;

/**
 * 拓展Launcher响应Socket通信回调接口，一般用在回调者不是Launcher对象
 */

public interface ExpandLauncherCallback {
    void onGetVisitorPicCfgResult(int configure);

    void onSetVisitorPicCfgResult(int result);
}
