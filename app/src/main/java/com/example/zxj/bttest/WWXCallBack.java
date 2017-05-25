package com.example.zxj.bttest;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import org.xutils.common.Callback.CommonCallback;

/**
 * 旺旺x3请求回掉
 *
 * @author xl
 * @date 2016-8-1 下午10:15:59
 * @description
 */
public abstract class WWXCallBack implements CommonCallback<String> {

	public static final String RESULT = "Result";
	/** 业务的数据字段 */
	private String modeKey;

	public WWXCallBack(String modeKey) {
		super();
		this.modeKey = modeKey;
	}
	@Override
	public void onCancelled(CancelledException arg0) {
	}

	@Override
	public void onError(Throwable arg0, boolean arg1) {
		Log.i("ERROR", "EORROR", (Exception) arg0);
	}

	@Override
	public void onFinished() {
		onAfterFinished();
	}

	@Override
	public void onSuccess(String result) {
		JSONObject data = JSON.parseObject(result).getJSONObject(
				modeKey + RESULT);
		ZLog.showPost(result);
		if (data != null) {
			boolean success = data.getBooleanValue("Success");
			if (success) {
				onAfterSuccessOk(data);
			} else {
				onAfterSuccessError(data);
			}
		}
	}

	public abstract void onAfterSuccessOk(JSONObject data);

	public abstract void onAfterFinished();

	public void onAfterSuccessError(JSONObject data) {
	}

}
