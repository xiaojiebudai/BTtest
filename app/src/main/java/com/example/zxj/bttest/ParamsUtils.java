package com.example.zxj.bttest;

import com.alibaba.fastjson.JSONObject;

import org.xutils.http.RequestParams;

public class ParamsUtils {
	/**
	 * post传json
	 * 
	 * @param jsonObject
	 * @param url
	 * @return
	 */
	public static RequestParams getPostJsonParams(JSONObject jsonObject,
                                                  String url) {
		RequestParams params = new RequestParams(url);
		params.setAsJsonContent(true);
		params.setBodyContent(jsonObject.toString());
		return params;
	}
}
