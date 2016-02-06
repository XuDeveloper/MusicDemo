package com.xu.musicdemo.model;

import android.widget.Button;

/*
 * ÎÄ×Ö°´Å¥
 */
public class WordButton {

	private int mIndex;
	private boolean mIsVisiable;
	private String mWordString;

	public Button mViewButton;

	public WordButton() {
		mIsVisiable = true;
		mWordString = "";
	}

	public int getmIndex() {
		return mIndex;
	}

	public void setmIndex(int mIndex) {
		this.mIndex = mIndex;
	}

	public boolean ismIsVisiable() {
		return mIsVisiable;
	}

	public void setmIsVisiable(boolean mIsVisiable) {
		this.mIsVisiable = mIsVisiable;
	}

	public String getmWordString() {
		return mWordString;
	}

	public void setmWordString(String mWordString) {
		this.mWordString = mWordString;
	}

}
