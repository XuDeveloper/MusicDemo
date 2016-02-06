package com.xu.musicdemo.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xu.musicdemo.R;
import com.xu.musicdemo.data.Const;
import com.xu.musicdemo.model.IWordButtonClickListener;
import com.xu.musicdemo.model.Song;
import com.xu.musicdemo.model.WordButton;
import com.xu.musicdemo.myui.MyGridView;
import com.xu.musicdemo.util.MyLog;
import com.xu.musicdemo.util.Util;

public class MainActivity extends Activity implements IWordButtonClickListener {

	public final static String TAG = "MainActivity";

	/** ��״̬-- ��ȷ */
	public final static int STATUS_ANSWER_RIGHT = 1;

	/** ��״̬-- ���� */
	public final static int STATUS_ANSWER_WRONG = 2;

	/** ��״̬-- ������ */
	public final static int STATUS_ANSWER_LACK = 3;

	// ��Ƭ��ض���(����淶����)
	private Animation mPanAnim;
	private LinearInterpolator mPanLin;

	private Animation mBarInAnim;
	private LinearInterpolator mBarInLin;

	private Animation mBarOutAnim;
	private LinearInterpolator mBarOutLin;

	private ImageView mViewPan;

	private ImageView mViewPanBar;

	// play �����¼�
	private ImageButton mBtnPlayStart;

	// �ж��Ƿ��ڲ���
	private boolean mIsRunning = false;

	// ���ֿ�����
	private ArrayList<WordButton> mAllWords;

	private ArrayList<WordButton> mBtnSelectWords;

	private MyGridView mMyGridView;

	// ��ѡ�����ֿ�UI����
	private LinearLayout mViewWordsContainer;

	// ��ǰ�ĸ���
	private Song mCurrentSong;

	// ��ǰ�ص�����
	private int mCurrentStageIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mViewPan = (ImageView) findViewById(R.id.imageView);
		mViewPanBar = (ImageView) findViewById(R.id.imageView3);

		mMyGridView = (MyGridView) findViewById(R.id.gridview);

		mMyGridView.setOnWordButtonClickListener(this);

		mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);

		// ��ʼ������
		mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mPanLin = new LinearInterpolator();
		mPanAnim.setInterpolator(mPanLin);
		mPanAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mViewPanBar.startAnimation(mBarOutAnim);
			}
		});

		mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
		mBarInLin = new LinearInterpolator();
		mBarInAnim.setFillAfter(true); // ���ֽ���״̬
		mBarInAnim.setInterpolator(mBarInLin);
		mBarInAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mViewPan.startAnimation(mPanAnim);
			}
		});

		mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
		mBarOutLin = new LinearInterpolator();
		mBarOutAnim.setFillAfter(true);
		mBarOutAnim.setInterpolator(mBarOutLin);
		mBarOutAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIsRunning = false;
				mBtnPlayStart.setVisibility(View.VISIBLE);
			}
		});

		mBtnPlayStart = (ImageButton) findViewById(R.id.btn_play_start);
		mBtnPlayStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HandlePlayButton();
			}
		});

		// ��ʼ������
		initCurrentStageData();

	}

	private void HandlePlayButton() {
		if (mViewPanBar != null) {
			if (!mIsRunning) {
				mIsRunning = true;
				mViewPanBar.startAnimation(mBarInAnim);
				mBtnPlayStart.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	protected void onPause() {
		mViewPan.clearAnimation(); // ������Ҫ��ͣ
		super.onPause();
	}

	private Song loadStageSongInfo(int stageIndex) {
		Song song = new Song();

		String[] stage = Const.SONG_INFO[stageIndex];
		song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
		song.setSongName(stage[Const.INDEX_SONG_NAME]);
		return song;
	}

	private void initCurrentStageData() {
		// ��ȡ��ǰ�صĸ�����Ϣ
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

		// ��ʼ����ѡ���
		mBtnSelectWords = initWordSelect();

		LayoutParams params = new LayoutParams(60, 60);

		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,
					params);
		}
		// �������
		mAllWords = initAllWord();
		// ����MyGridView����
		mMyGridView.updateData(mAllWords);
	}

	/**
	 * 
	 * ��ʼ����ѡ���ֿ�
	 */
	private ArrayList<WordButton> initAllWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();

		// ������д�ѡ����
		String[] words = generateWords();

		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			WordButton button = new WordButton();
			button.setmWordString(words[i]);
			data.add(button);
		}
		return data;
	}

	/**
	 * 
	 * ��ʼ����ѡ���ֿ�
	 */
	private ArrayList<WordButton> initWordSelect() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();

		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			View view = Util.getView(MainActivity.this,
					R.layout.self_ui_gridview_item);

			final WordButton holder = new WordButton();

			holder.mViewButton = (Button) view.findViewById(R.id.item_btn);
			holder.mViewButton.setTextColor(Color.WHITE);
			holder.mViewButton.setText("");
			holder.setmIsVisiable(false);

			holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
			holder.mViewButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					clearTheAnswer(holder);
				}
			});
			data.add(holder);
		}
		return data;
	}

	@Override
	public void onWordButtonClick(WordButton wordButton) {
		setSelectWord(wordButton);
		
		// ��ô�״̬
		int checkResult = checkTheAnswer();
		
		// ����
		if (checkResult == STATUS_ANSWER_RIGHT) {
			
		} else if (checkResult == STATUS_ANSWER_WRONG) {
			
		} else if (checkResult == STATUS_ANSWER_LACK) {
			
		}
	}

	private void clearTheAnswer(WordButton wordButton) {
		wordButton.mViewButton.setText("");
		wordButton.setmWordString("");
		wordButton.setmIsVisiable(false);

		// ���ô�ѡ��ɼ���
		setButtonVisiable(mAllWords.get(wordButton.getmIndex()), View.VISIBLE);
	}

	/**
	 * ���ô�
	 * 
	 * @param wordButton
	 */
	private void setSelectWord(WordButton wordButton) {
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).getmWordString().equals("")) {
				// ���ô����ֿ����ݼ��ɼ���
				mBtnSelectWords.get(i).mViewButton.setText(wordButton
						.getmWordString());
				mBtnSelectWords.get(i).setmIsVisiable(true);
				mBtnSelectWords.get(i).setmWordString(
						wordButton.getmWordString());
				// ��¼����
				mBtnSelectWords.get(i).setmIndex(wordButton.getmIndex());

				MyLog.d(TAG, mBtnSelectWords.get(i).getmIndex() + "");

				// ���ô�ѡ��ɼ���
				setButtonVisiable(wordButton, View.INVISIBLE);

				break;
			}
		}
	}

	/**
	 * ���ô�ѡ���ֿ��Ƿ�ɼ�
	 * 
	 * @param wordButton
	 * @param visibility
	 */
	private void setButtonVisiable(WordButton wordButton, int visibility) {
		wordButton.mViewButton.setVisibility(visibility);
		wordButton.setmIsVisiable((visibility == View.VISIBLE) ? true : false);

		MyLog.d(TAG, wordButton.ismIsVisiable() + "");
	}

	/**
	 * �������еĴ�ѡ����
	 * 
	 * @return
	 */
	private String[] generateWords() {
		Random random = new Random();

		String[] words = new String[MyGridView.COUNTS_WORDS];

		// �������
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			words[i] = mCurrentSong.getNameCharacters()[i] + "";
		}

		// ��ȡ������ִ�������
		for (int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; i++) {
			words[i] = getRandomChar() + "";
		}

		// ��������˳��
		for (int i = MyGridView.COUNTS_WORDS - 1; i >= 0; i--) {
			int index = random.nextInt(i + 1);

			String buf = words[index];
			words[index] = words[i];
			words[i] = buf;
		}

		return words;
	}

	/**
	 * �����������
	 * 
	 * @return
	 */
	private char getRandomChar() {
		String str = "";
		int highPos;
		int lowPos;

		Random random = new Random();
		highPos = (176 + Math.abs(random.nextInt(39)));
		lowPos = (160 + Math.abs(random.nextInt(93)));

		byte[] b = new byte[2];
		b[0] = (Integer.valueOf(highPos)).byteValue();
		b[1] = (Integer.valueOf(lowPos)).byteValue();

		try {
			str = new String(b, "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return str.charAt(0);
	}

	/**
	 * ����
	 * 
	 * @return
	 */
	private int checkTheAnswer() {
		// ��鳤��
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			// ����пյģ�˵���𰸲�����
			if (mBtnSelectWords.get(i).getmWordString().length() == 0) {
				return STATUS_ANSWER_LACK;
			}
		}

		// ��������������ȷ��
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			sb.append(mBtnSelectWords.get(i).getmWordString());
		}

		return (sb.toString().equals(mCurrentSong.getSongName())) ? STATUS_ANSWER_RIGHT
				: STATUS_ANSWER_WRONG;
	}
}
