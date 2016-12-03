package com.dongdong.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dongdong.ui.dialog.CommonDialog;
import com.jr.door.R;

/**
 * 对话框辅助类
 * Created by 火蚁 on 15/6/19.
 */
public class DialogHelp {

    /***
     * 获取一个耗时等待对话框
     *
     * @param context
     * @param message
     * @return ProgressDialog
     */
    public static CommonDialog getProgressDialog(Context context, String message) {
        CommonDialog dialog = new CommonDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        dialog.setContent(view);
        TextView showText = (TextView) view.findViewById(R.id.tipTextView);
        if (TextUtils.isEmpty(message))
            showText.setText(message);
        dialog.setCancelable(true);
        return dialog;
    }

    public static CommonDialog getMessageDialog(Context context, int strId) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setTitle(R.string.tip_title);
        dialog.setMessage(strId);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.setCancelable(true);
        return dialog;
    }
}
