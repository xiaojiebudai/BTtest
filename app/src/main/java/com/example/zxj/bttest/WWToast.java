package com.example.zxj.bttest;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;


/**
 * Toast类
 * 
 * @author xl
 * @date:2016-7-25下午12:54:02
 * @description
 */
public class WWToast {
	/**
	 * 短时间显示Toast
	 * 

	 * @param message
	 */
	public static void showShort(String message) {
			Toast	toast = Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_SHORT);
			 toast.setGravity(Gravity.CENTER, 0, 0);

		toast.setText(message);
		toast.show();
	}




}
