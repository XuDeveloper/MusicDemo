package com.xu.musicdemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

public class Util {

	public static View getView(Context context, int layoutId) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(layoutId, null);
		return view;
	}
	
	/**
	 * ������ת
	 * @param context
	 * @param cls
	 */
	public static void startActivity(Context context, Class cls) {
		Intent intent = new Intent();
		intent.setClass(context, cls);
		context.startActivity(intent);
		
		//�رյ�ǰ��Activity
		((Activity)context).finish();
	}
	
}
