package com.xu.musicdemo.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xu.musicdemo.R;
import com.xu.musicdemo.data.Const;
import com.xu.musicdemo.model.IAlertDialogButtonListener;

public class Util {

	private static AlertDialog mAlertDialog;

	public static View getView(Context context, int layoutId) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(layoutId, null);
		return view;
	}

	/**
	 * 界面跳转
	 * 
	 * @param context
	 * @param cls
	 */
	public static void startActivity(Context context, Class cls) {
		Intent intent = new Intent();
		intent.setClass(context, cls);
		context.startActivity(intent);

		// 关闭当前的Activity
		((Activity) context).finish();
	}

	/**
	 * 显示对话框
	 * 
	 * @param context
	 * @param message
	 * @param listener
	 */
	public static void showDialog(final Context context, String message,
			final IAlertDialogButtonListener listener) {
		View dialogView = null;

		AlertDialog.Builder builder = new AlertDialog.Builder(context,
				R.style.Theme_Transparent);
		dialogView = getView(context, R.layout.dialog_view);

		ImageButton btnOkView = (ImageButton) dialogView
				.findViewById(R.id.btn_dialog_ok);
		ImageButton btnCancelView = (ImageButton) dialogView
				.findViewById(R.id.btn_dialog_cancel);
		TextView txtMessageView = (TextView) dialogView
				.findViewById(R.id.text_dialog_message);

		txtMessageView.setText(message);

		btnOkView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 关闭对话框
				if (mAlertDialog != null) {
					mAlertDialog.cancel();
				}

				// 事件回调
				if (listener != null) {
					listener.onClick();
				}

				// 播放音效
				MyPlayer.playTone(context, MyPlayer.INDEX_STONE_ENTER);
			}
		});

		btnCancelView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 关闭对话框
				if (mAlertDialog != null) {
					mAlertDialog.cancel();
				}

				// 播放音效
				MyPlayer.playTone(context, MyPlayer.INDEX_STONE_CANCEL);
			}
		});

		// 为Dialog设置View
		builder.setView(dialogView);
		mAlertDialog = builder.create();

		// 显示对话框
		mAlertDialog.show();
	}
	
	/**
	 * 游戏数据保存
	 * @param context
	 * @param stageIndex
	 * @param coins
	 */
	public static void saveData(Context context, int stageIndex, int coins) {
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(Const.FILE_NAME_SAVE_DATA, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(fos);
			
			dos.writeInt(stageIndex);
			dos.writeInt(coins);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 读取游戏数据
	 * @param context
	 * @return
	 */
	public static int[] loadData(Context context) {
		FileInputStream fis = null;
		int[] datas = {-1, Const.TOTAL_COINS};
		try {
			fis = context.openFileInput(Const.FILE_NAME_SAVE_DATA);
			DataInputStream dis = new DataInputStream(fis);
			datas[Const.INDEX_LOAD_DATA_STAGE] = dis.readInt();
			datas[Const.INDEX_LOAD_DATA_COINS] = dis.readInt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return datas;
	}

}
