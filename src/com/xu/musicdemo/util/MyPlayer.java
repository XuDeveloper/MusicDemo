package com.xu.musicdemo.util;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

/**
 * ���ֲ�����
 */
public class MyPlayer {

	// ����
	public static final int INDEX_STONE_ENTER = 0;
	public static final int INDEX_STONE_CANCEL = 1;
	public static final int INDEX_STONE_COIN = 2;

	// ��Ч���ļ���
	private static final String[] SONG_NAMES = { "enter.mp3", "cancel.mp3",
			"coin.mp3" };

	// ��Ч
	private static MediaPlayer[] mToneMediaPlayer = new MediaPlayer[SONG_NAMES.length];

	// ��������
	private static MediaPlayer mMusicMediaPlayer;

	/**
	 * ������Ч
	 * 
	 * @param context
	 * @param index
	 */
	public static void playTone(Context context, int index) {
		// ��������
		AssetManager assetManager = context.getAssets();

		if (mToneMediaPlayer[index] == null) {
			mToneMediaPlayer[index] = new MediaPlayer();

			try {
				AssetFileDescriptor fileDescriptor = assetManager
						.openFd(SONG_NAMES[index]);
				mToneMediaPlayer[index].setDataSource(
						fileDescriptor.getFileDescriptor(),
						fileDescriptor.getStartOffset(),
						fileDescriptor.getLength());
				mToneMediaPlayer[index].prepare();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		mToneMediaPlayer[index].start();
	}

	/**
	 * ���Ÿ���
	 * 
	 * @param context
	 * @param fileName
	 */
	public static void playSong(Context context, String fileName) {
		if (mMusicMediaPlayer == null) {
			mMusicMediaPlayer = new MediaPlayer();
		}

		// ǿ������
		mMusicMediaPlayer.reset();

		// ���������ļ�
		AssetManager assetManager = context.getAssets();
		try {
			AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
			mMusicMediaPlayer
					.setDataSource(fileDescriptor.getFileDescriptor(),
							fileDescriptor.getStartOffset(),
							fileDescriptor.getLength());

			mMusicMediaPlayer.prepare();

			// ��������
			mMusicMediaPlayer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stopTheSong(Context context) {
		if (mMusicMediaPlayer != null) {
			mMusicMediaPlayer.stop();
		}
	}
}
