package com.example.zxj.bttest;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 弹窗工具类
 *
 * @author xl
 * @date:2016-8-2下午4:49:53
 * @description
 */
public class DialogUtils {

	/**
	 * 等待弹窗
	 */
	public static Dialog getWaitDialog(Activity context, boolean cancelable) {
		final Dialog dialog = new Dialog(context, R.style.DialogStyle);

		dialog.setContentView(R.layout.custom_progress_dialog);
		dialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dialog.dismiss();
					return true;
				}
				return false;
			}
		});
		dialog.setCancelable(cancelable);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		int screenW = context.getResources().getDisplayMetrics().widthPixels;
		lp.width = (int) (0.6 * screenW);
		TextView titleTxtv = (TextView) dialog.findViewById(R.id.dialogText);
		titleTxtv.setText("请稍后");
		return dialog;
	}



}
