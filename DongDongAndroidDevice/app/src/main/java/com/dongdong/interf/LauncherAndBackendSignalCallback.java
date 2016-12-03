package com.dongdong.interf;

import com.dongdong.socket.normal.InfoNetParam;

/**
 * 后台页面获取Linux核心板信息接口
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public interface LauncherAndBackendSignalCallback {

    int onGetNetResult(int cmdflag, InfoNetParam netparam);

    int onSetNetResult(int cmdflag, int result);
}
