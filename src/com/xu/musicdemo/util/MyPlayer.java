package com.xu.musicdemo.util;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

/**
 * 音乐播放类
 */
public class MyPlayer {

	// 索引
	public static final int INDEX_STONE_ENTER = 0;
	public static final int INDEX_STONE_CANCEL = 1;
	public static final int INDEX_STONE_COIN = 2;

	// 音效的文件名
	private static final String[] SONG_NAMES = { "enter.mp3", "cancel.mp3",
			"coin.mp3" };

	// 音效
	private static MediaPlayer[] mToneMediaPlayer = new MediaPlayer[SONG_NAMES.length];

	// 歌曲播放
	private static MediaPlayer mMusicMediaPlayer;

	/**
	 * 播放音效
	 * 
	 * @param context
	 * @param index
	 */
	public static void playTone(Context context, int index) {
		// 加载声音
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
	 * 播放歌曲
	 * 
	 * @param context
	 * @param fileName
	 */
	public static void playSong(Context context, String fileName) {
		if (mMusicMediaPlayer == null) {
			mMusicMediaPlayer = new MediaPlayer();
		}

		// 强制重置
		mMusicMediaPlayer.reset();

		// 加载声音文件
		AssetManager assetManager = context.getAssets();
		try {
			AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
			mMusicMediaPlayer
					.setDataSource(fileDescriptor.getFileDescriptor(),
							fileDescriptor.getStartOffset(),
							fileDescriptor.getLength());

			mMusicMediaPlayer.prepare();

			// 声音播放
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
