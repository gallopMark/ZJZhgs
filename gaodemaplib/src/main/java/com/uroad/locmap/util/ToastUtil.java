/**
 * 
 */
package com.uroad.locmap.util;

import android.R;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {

	public static void show(Context context, String info) {
		Toast.makeText(context, info, Toast.LENGTH_LONG).show();
	}

	public static void show(Context context, int info) {
		Toast.makeText(context, info, Toast.LENGTH_LONG).show();
	}
	
	private static Toast mToast;

	private static Handler mHandler = new Handler();
	private static Runnable r = new Runnable() {
		public void run() {
			if (mToast != null) {
				mToast.cancel();
				mToast = null;// toast隐藏后，将其置为null
			}
		}
	};

	public static void showShortToast(Context context, String message) {
		TextView text = new TextView(context);// 显示的提示文字
		text.setText(message);
		text.setBackgroundColor(Color.BLACK);
		text.setTextColor(context.getResources().getColor(R.color.white));
		text.setPadding(30, 30, 30, 30);
		if (mToast != null) {//
			mHandler.postDelayed(r, 0);// 隐藏toast
		} else {
			mToast = new Toast(context);
			mToast.setDuration(Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.BOTTOM, 0, 150);
			mToast.setView(text);
		}

		mHandler.postDelayed(r, 1000);// 延迟1秒隐藏toast
		mToast.show();
	}
}
