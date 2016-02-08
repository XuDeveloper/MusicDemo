package com.xu.musicdemo.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.TextView;

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

	// ��˸����
	public final static int SPASH_TIMES = 6;

	// ��Ƭ��ض���(����淶����)
	private Animation mPanAnim;
	private LinearInterpolator mPanLin;

	private Animation mBarInAnim;
	private LinearInterpolator mBarInLin;

	private Animation mBarOutAnim;
	private LinearInterpolator mBarOutLin;

	// ��Ƭ�ؼ�
	private ImageView mViewPan;

	// ���˿ؼ�
	private ImageView mViewPanBar;

	// ��ǰ������
	private TextView mCurrentStagePassView;

	private TextView mCurrentStageView;

	// ��ǰ��������
	private TextView mCurrentSongNamePassView;

	// play �����¼�
	private ImageButton mBtnPlayStart;

	// ���ؽ���
	private View mPassView;

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

	// ��ǰ��ҵ�����
	private int mCurrentCoins = Const.TOTAL_COINS;

	// ��ҵ�View
	private TextView mViewCurrentCoins;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mViewPan = (ImageView) findViewById(R.id.imageView);
		mViewPanBar = (ImageView) findViewById(R.id.imageView3);

		mMyGridView = (MyGridView) findViewById(R.id.gridview);

		mViewCurrentCoins = (TextView) findViewById(R.id.txt_bar_coins);
		mViewCurrentCoins.setText(mCurrentCoins + "");

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

		// ����ɾ�������¼�
		handleDeleteWord();

		// ������ʾ�����¼�
		handleTipAnswer();
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

	/**
	 * ���ص�ǰ�ص�����
	 */
	private void initCurrentStageData() {
		// ��ȡ��ǰ�صĸ�����Ϣ
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

		// ��ʼ����ѡ���
		mBtnSelectWords = initWordSelect();

		LayoutParams params = new LayoutParams(60, 60);

		// ���ԭ���Ĵ�
		mViewWordsContainer.removeAllViews();

		// �����µĴ𰸿�
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,
					params);
		}

		// ��ʾ��ǰ�ص�����
		mCurrentStageView = (TextView) findViewById(R.id.text_current_stage);
		if (mCurrentStageView != null) {
			mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
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
			// ���ز���ý���
			handlePassEvent();
		} else if (checkResult == STATUS_ANSWER_WRONG) {
			// ��˸���ֲ���ʾ�û�
			sparkTheWords();
		} else if (checkResult == STATUS_ANSWER_LACK) {
			// ����������ɫΪ��ɫ
			for (int i = 0; i < mBtnSelectWords.size(); i++) {
				mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
		}
	}

	/**
	 * ������ؽ��漰�¼�
	 */
	private void handlePassEvent() {
		// ��ʾ���ؽ���
		mPassView = (LinearLayout) this.findViewById(R.id.pass_view);
		mPassView.setVisibility(View.VISIBLE);

		// ֹͣδ��ɵĶ���
		mViewPan.clearAnimation();

		// ��ǰ�ص�����
		mCurrentStagePassView = (TextView) findViewById(R.id.text_current_stage_pass);
		if (mCurrentStagePassView != null) {
			mCurrentStagePassView.setText((mCurrentStageIndex + 1) + "");
		}

		// ��ʾ��������
		mCurrentSongNamePassView = (TextView) findViewById(R.id.text_current_song_name_pass);
		if (mCurrentSongNamePassView != null) {
			mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
		}

		// ��һ�ذ�������
		ImageButton btnPass = (ImageButton) findViewById(R.id.btn_next);
		btnPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (judgeAppPassed()) {
					// ���뵽ͨ�ؽ���
					Util.startActivity(MainActivity.this, AllPassView.class);
				} else {
					// ��ʼ��һ��
					mPassView.setVisibility(View.GONE);

					// ���عؿ�����
					initCurrentStageData();
				}
			}
		});
	}

	/**
	 * �ж��Ƿ�ͨ��
	 * 
	 * @return
	 */
	private boolean judgeAppPassed() {
		return mCurrentStageIndex == (Const.SONG_INFO.length - 1);
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

	/**
	 * ������˸
	 */
	private void sparkTheWords() {
		// ��ʱ�����
		TimerTask task = new TimerTask() {
			boolean mChange = false;
			int mSparedTimes = 0;

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (++mSparedTimes > SPASH_TIMES) {
							return;
						}

						// ִ����˸�߼���������ʾ��ɫ�Ͱ�ɫ������
						for (int i = 0; i < mBtnSelectWords.size(); i++) {
							mBtnSelectWords.get(i).mViewButton
									.setTextColor(mChange ? Color.RED
											: Color.WHITE);
						}
						mChange = !mChange;
					}
				});
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 1, 150);

	}

	/**
	 * �Զ�ѡ��һ����
	 */
	private void tipAnswer() {
		boolean tipWord = false;
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).getmWordString().length() == 0) {
				// ���ݵ�ǰ�Ĵ𰸿�����ѡ���Ӧ�����ֲ�����
				onWordButtonClick(findIsAnswerWord(i));

				tipWord = true;

				// ���ٽ��
				if (!handleCoins(-getTipCoins())) {
					// ��Ҳ�������ʾ��ʾ�Ի���
					return;
				}
				break;
			}
		}

		// û���ҵ��������Ĵ�
		if (!tipWord) {
			// ��˸������ʾ�û�
			sparkTheWords();
		}

	}

	/**
	 * ɾ������
	 */
	private void deleteOneWord() {
		// ���ٽ��
		if (!handleCoins(-getDeleteWordCoins())) {
			// ��Ҳ�������ʾ��ʾ�Ի���
			return;
		}

		// �����������Ӧ��wordButton����Ϊ���ɼ�
		setButtonVisiable(findNotAnswerWord(), View.INVISIBLE);
	}

	/**
	 * �ҵ�һ�����Ǵ𰸵��ļ������ҵ�ǰ�ǿɼ���
	 * 
	 * @return
	 */
	private WordButton findNotAnswerWord() {
		Random random = new Random();
		WordButton buf = null;
		while (true) {
			int index = random.nextInt(MyGridView.COUNTS_WORDS);

			buf = mAllWords.get(index);

			if (buf.ismIsVisiable() && !isTheAnswerWord(buf)) {
				return buf;
			}

		}
	}

	/**
	 * �ҵ�һ��������,indexΪ����
	 * 
	 * @return
	 */
	private WordButton findIsAnswerWord(int index) {
		WordButton buf = null;

		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			buf = mAllWords.get(i);

			if (buf.getmWordString().equals(
					"" + mCurrentSong.getNameCharacters()[index])) {
				return buf;
			}
		}
		return null;
	}

	/**
	 * �ж�ĳ�������Ƿ�Ϊ��
	 * 
	 * @param wordButton
	 * @return
	 */
	private boolean isTheAnswerWord(WordButton wordButton) {
		boolean result = false;
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			if (wordButton.getmWordString().equals(
					"" + mCurrentSong.getNameCharacters()[i])) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * ���ӻ��߼���ָ�������Ľ��
	 * 
	 * @param data
	 * @return true ����/���ٳɹ� false ʧ��
	 */
	private boolean handleCoins(int data) {
		// �жϵ�ǰ�ܵĽ�������Ƿ�ɼ���
		if (mCurrentCoins + data >= 0) {
			mCurrentCoins += data;

			mViewCurrentCoins.setText(mCurrentCoins + "");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * �������ļ����ȡɾ��������Ҫ�õĽ��
	 * 
	 * @return
	 */
	private int getDeleteWordCoins() {
		return getResources().getInteger(R.integer.pay_delete_answer);
	}

	/**
	 * �������ļ����ȡ��ʾ������Ҫ�õĽ��
	 * 
	 * @return
	 */
	private int getTipCoins() {
		return getResources().getInteger(R.integer.pay_tip_answer);
	}

	/**
	 * ����ɾ����ѡ���ֵ��¼�
	 */
	private void handleDeleteWord() {
		ImageButton button = (ImageButton) findViewById(R.id.btn_delete_word);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteOneWord();
			}
		});
	}

	/**
	 * ������ʾ�����¼�
	 */
	private void handleTipAnswer() {
		ImageButton button = (ImageButton) findViewById(R.id.btn_tip_answer);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tipAnswer();
			}
		});
	}

}
