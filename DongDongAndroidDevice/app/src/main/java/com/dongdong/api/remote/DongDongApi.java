package com.dongdong.api.remote;

import com.dongdong.api.ApiHttpClient;
import com.dongdong.utils.DDLog;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class DongDongApi {

    public static void checkUpdate(AsyncHttpResponseHandler handler) {
        String path = DDLog.isDebug ? "dd/androiddev/debug/version" : "dd/androiddev/version";
        ApiHttpClient.get(path, handler);
    }
}
