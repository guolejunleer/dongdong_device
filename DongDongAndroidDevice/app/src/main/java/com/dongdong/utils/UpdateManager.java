package com.dongdong.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dongdong.AppConfig;
import com.dongdong.api.remote.DongDongApi;
import com.dongdong.base.BaseApplication;
import com.dongdong.ui.dialog.CommonDialog;
import com.jr.door.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.msebera.android.httpclient.Header;

public class UpdateManager {

    private Context mContext;

    private boolean isShow = false;

    private CommonDialog mWaitDialog;
    private String mPltVerName;

    private boolean canceled;

    private final String baseSaveFileName = AppConfig.DEFAULT_SAVE_FILE_PATH;
    private final String urlPath = DDLog.isDebug ? "http://www.dd121.com/dd/androiddev/debug/dd121_debug.apk"
            : "http://www.dd121.com/dd/androiddev/dd121.apk";
    private final String downloadFilename = baseSaveFileName + urlPath.substring(urlPath.lastIndexOf("/") + 1);

    public UpdateManager(Context context, boolean isShow) {
        this.mContext = context;
        this.isShow = isShow;
    }

    private AsyncHttpResponseHandler mCheckUpdateHandle = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int statusCode, Header[] arg1, byte[] arg2,
                              Throwable error) {
            hideCheckDialog();
            if (isShow) {
                showFaileDialog();
            }
            DDLog.d("UpdateManager.clazz--->>> onFailure error:" + error);
        }

        @Override
        public void onSuccess(int statusCode, Header[] arg1, byte[] responseBody) {
            hideCheckDialog();
            String result = new String(responseBody);
            String[] strArray = result.split("[+]");
            mPltVerName = strArray[1].trim();
            DDLog.d("UpdateManager.clazz--->>>onSuccess result:" + result
                    + ";mPltVerName:" + mPltVerName + ";");
            onFinshCheck();
        }
    };

    public boolean haveNewVersion() {
        boolean haveNew = false;
        String curVerName = DeviceInfoUtils.getVersionName(mContext).trim();
        int result = curVerName.compareTo(mPltVerName);
        DDLog.d("UpdataManager.clazz-->>>haveNewVersion curVerName " + curVerName
                + ";mPltVerName:" + mPltVerName + "; result:" + result);
        if (result < 0) {
            haveNew = true;
        }
        return haveNew;
    }

    public void checkUpdate() {
        if (isShow) {
            showCheckAppVerDialog();
        }
        DongDongApi.checkUpdate(mCheckUpdateHandle);
    }

    private void onFinshCheck() {
        if (haveNewVersion()) {
            showUpdateInfo();
        } else {
            if (isShow) {
                showLatestDialog();
            }
        }
    }

    private void showCheckAppVerDialog() {
        if (mWaitDialog == null) {
            mWaitDialog = DialogHelp.getProgressDialog((Activity) mContext,
                    BaseApplication.resources().getString(R.string.loading_tip));
        }
        mWaitDialog.show();
    }

    private void hideCheckDialog() {
        if (mWaitDialog != null) {
            mWaitDialog.dismiss();
        }
    }

    private void showUpdateInfo() {
        CommonDialog updataDialog = new CommonDialog(mContext);
        updataDialog.setTitle(R.string.update_app_version);
        updataDialog.setMessage(String.format(mContext.
                getString(R.string.find_app_version), mPltVerName));
        updataDialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DownLoadAsyncTask().execute(urlPath, downloadFilename);
                dialog.dismiss();
            }
        });
        updataDialog.setNegativeButton(R.string.cancel, null);
        updataDialog.setCancelable(false);
        updataDialog.show();
    }

    private void showLatestDialog() {
        DialogHelp.getMessageDialog(mContext, R.string.last_app_version).show();
    }

    private void showFaileDialog() {
        DialogHelp.getMessageDialog(mContext, R.string.failed_get_app_version).show();
    }

    private class DownLoadAsyncTask extends AsyncTask<String, Integer, Boolean> {

        CommonDialog progressDialog;
        TextView showText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DDLog.d("UpdateManager.clazz --->>>onPreExecute....... ");
            progressDialog = new CommonDialog(mContext);
            View view = LayoutInflater.from(mContext).inflate(R.layout.loading_dialog, null);
            progressDialog.setContent(view);
            showText = (TextView) view.findViewById(R.id.tipTextView);
            showText.setText(R.string.start_download);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            File file = new File(AppConfig.DEFAULT_SAVE_FILE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            File saveFile = new File(downloadFilename);

            int downloadCount = 0;
            int currentSize = 0;
            long totalSize = 0;
            int updateTotalSize = 0;

            HttpURLConnection httpConnection = null;
            InputStream is = null;
            FileOutputStream fos = null;

            try {
                URL url = new URL(urlPath);
                DDLog.d("UpdateManager.clazz doingbackground -->>> urlPath:" + urlPath
                        + "; downloadFilename:" + downloadFilename + "; baseSaveFileName:" + baseSaveFileName);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection
                        .setRequestProperty("User-Agent", "PacificHttpClient");
                if (currentSize > 0) {
                    httpConnection.setRequestProperty("RANGE", "bytes="
                            + currentSize + "-");
                }
                httpConnection.setConnectTimeout(10000);
                httpConnection.setReadTimeout(20000);
                updateTotalSize = httpConnection.getContentLength();
                if (httpConnection.getResponseCode() == 404) {
                    throw new Exception("fail!");
                }
                is = httpConnection.getInputStream();
                fos = new FileOutputStream(saveFile, false);
                byte buffer[] = new byte[1024];
                int readsize;
                while ((readsize = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, readsize);
                    totalSize += readsize;
                    // 为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
//                    if ((downloadCount == 0)
//                            || (int) (totalSize * 100 / updateTotalSize) - 10 >= downloadCount) {
//                        downloadCount += 10;
//                        // 更新进度
//                        publishProgress(downloadCount);
//                    }
                    downloadCount = (int) (totalSize * 100 / updateTotalSize);
                    publishProgress(downloadCount);
                }

                // 下载完成通知安装
                // 下载完了，cancelled也要设置
                canceled = true;

            } catch (Exception e) {
                DDLog.d("UpdateManager.clazz --->>> download apk had faild " + e);
                e.printStackTrace();
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return canceled;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            showText.setText(String.format(BaseApplication.resources().
                    getString(R.string.loading_state_tip), values[0]));
        }

        @Override
        protected void onPostExecute(Boolean shoulInstall) {
            DDLog.d("UpdateManager.clazz --->>>onPostExecute shoulInstall "
                    + shoulInstall);
            if (shoulInstall) {
                installApk();
            } else {
                BaseApplication.showToast("下载失败啦!!!");
            }
            progressDialog.dismiss();
        }
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File apkfile = new File(downloadFilename);
        if (!apkfile.exists()) {
            return;
        }
        DeviceInfoUtils.installAPK(mContext, apkfile);
    }
}
