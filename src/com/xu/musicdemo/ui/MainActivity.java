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

	/** 答案状态-- 正确 */
	public final static int STATUS_ANSWER_RIGHT = 1;

	/** 答案状态-- 错误 */
	public final static int STATUS_ANSWER_WRONG = 2;

	/** 答案状态-- 不完整 */
	public final static int STATUS_ANSWER_LACK = 3;

	// 闪烁次数
	public final static int SPASH_TIMES = 6;

	// 唱片相关动画(代码规范！！)
	private Animation mPanAnim;
	private LinearInterpolator mPanLin;

	private Animation mBarInAnim;
	private LinearInterpolator mBarInLin;

	private Animation mBarOutAnim;
	private LinearInterpolator mBarOutLin;

	// 唱片控件
	private ImageView mViewPan;

	// 播杆控件
	private ImageView mViewPanBar;

	// 当前关索引
	private TextView mCurrentStagePassView;

	private TextView mCurrentStageView;

	// 当前歌曲名称
	private TextView mCurrentSongNamePassView;

	// play 按键事件
	private ImageButton mBtnPlayStart;

	// 过关界面
	private View mPassView;

	// 判断是否在播放
	private boolean mIsRunning = false;

	// 文字框容器
	private ArrayList<WordButton> mAllWords;

	private ArrayList<WordButton> mBtnSelectWords;

	private MyGridView mMyGridView;

	// 已选择文字框UI容器
	private LinearLayout mViewWordsContainer;

	// 当前的歌曲
	private Song mCurrentSong;

	// 当前关的索引
	private int mCurrentStageIndex = -1;

	// 当前金币的数量
	private int mCurrentCoins = Const.TOTAL_COINS;

	// 金币的View
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

		// 初始化动画
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
		mBarInAnim.setFillAfter(true); // 保持结束状态
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

		// 初始化数据
		initCurrentStageData();

		// 处理删除按键事件
		handleDeleteWord();

		// 处理提示按键事件
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
		mViewPan.clearAnimation(); // 动画需要暂停
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
	 * 加载当前关的数据
	 */
	private void initCurrentStageData() {
		// 读取当前关的歌曲信息
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

		// 初始化已选择框
		mBtnSelectWords = initWordSelect();

		LayoutParams params = new LayoutParams(60, 60);

		// 清空原来的答案
		mViewWordsContainer.removeAllViews();

		// 增加新的答案框
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,
					params);
		}

		// 显示当前关的索引
		mCurrentStageView = (TextView) findViewById(R.id.text_current_stage);
		if (mCurrentStageView != null) {
			mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
		}

		// 获得数据
		mAllWords = initAllWord();
		// 更新MyGridView数据
		mMyGridView.updateData(mAllWords);
	}

	/**
	 * 
	 * 初始化待选文字框
	 */
	private ArrayList<WordButton> initAllWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();

		// 获得所有待选文字
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
	 * 初始化已选文字框
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

		// 获得答案状态
		int checkResult = checkTheAnswer();

		// 检查答案
		if (checkResult == STATUS_ANSWER_RIGHT) {
			// 过关并获得奖励
			handlePassEvent();
		} else if (checkResult == STATUS_ANSWER_WRONG) {
			// 闪烁文字并提示用户
			sparkTheWords();
		} else if (checkResult == STATUS_ANSWER_LACK) {
			// 设置文字颜色为白色
			for (int i = 0; i < mBtnSelectWords.size(); i++) {
				mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
		}
	}

	/**
	 * 处理过关界面及事件
	 */
	private void handlePassEvent() {
		// 显示过关界面
		mPassView = (LinearLayout) this.findViewById(R.id.pass_view);
		mPassView.setVisibility(View.VISIBLE);

		// 停止未完成的动画
		mViewPan.clearAnimation();

		// 当前关的索引
		mCurrentStagePassView = (TextView) findViewById(R.id.text_current_stage_pass);
		if (mCurrentStagePassView != null) {
			mCurrentStagePassView.setText((mCurrentStageIndex + 1) + "");
		}

		// 显示歌曲名称
		mCurrentSongNamePassView = (TextView) findViewById(R.id.text_current_song_name_pass);
		if (mCurrentSongNamePassView != null) {
			mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
		}

		// 下一关按键处理
		ImageButton btnPass = (ImageButton) findViewById(R.id.btn_next);
		btnPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (judgeAppPassed()) {
					// 进入到通关界面
					Util.startActivity(MainActivity.this, AllPassView.class);
				} else {
					// 开始新一关
					mPassView.setVisibility(View.GONE);

					// 加载关卡数据
					initCurrentStageData();
				}
			}
		});
	}

	/**
	 * 判断是否通关
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

		// 设置待选框可见性
		setButtonVisiable(mAllWords.get(wordButton.getmIndex()), View.VISIBLE);
	}

	/**
	 * 设置答案
	 * 
	 * @param wordButton
	 */
	private void setSelectWord(WordButton wordButton) {
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).getmWordString().equals("")) {
				// 设置答案文字框内容及可见性
				mBtnSelectWords.get(i).mViewButton.setText(wordButton
						.getmWordString());
				mBtnSelectWords.get(i).setmIsVisiable(true);
				mBtnSelectWords.get(i).setmWordString(
						wordButton.getmWordString());
				// 记录索引
				mBtnSelectWords.get(i).setmIndex(wordButton.getmIndex());

				MyLog.d(TAG, mBtnSelectWords.get(i).getmIndex() + "");

				// 设置待选框可见性
				setButtonVisiable(wordButton, View.INVISIBLE);

				break;
			}
		}
	}

	/**
	 * 设置待选文字框是否可见
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
	 * 生成所有的待选文字
	 * 
	 * @return
	 */
	private String[] generateWords() {
		Random random = new Random();

		String[] words = new String[MyGridView.COUNTS_WORDS];

		// 存入歌名
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			words[i] = mCurrentSong.getNameCharacters()[i] + "";
		}

		// 获取随机文字存入数组
		for (int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; i++) {
			words[i] = getRandomChar() + "";
		}

		// 打乱文字顺序
		for (int i = MyGridView.COUNTS_WORDS - 1; i >= 0; i--) {
			int index = random.nextInt(i + 1);

			String buf = words[index];
			words[index] = words[i];
			words[i] = buf;
		}

		return words;
	}

	/**
	 * 生成随机汉字
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
	 * 检查答案
	 * 
	 * @return
	 */
	private int checkTheAnswer() {
		// 检查长度
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			// 如果有空的，说明答案不完整
			if (mBtnSelectWords.get(i).getmWordString().length() == 0) {
				return STATUS_ANSWER_LACK;
			}
		}

		// 答案完整，则检查正确性
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			sb.append(mBtnSelectWords.get(i).getmWordString());
		}

		return (sb.toString().equals(mCurrentSong.getSongName())) ? STATUS_ANSWER_RIGHT
				: STATUS_ANSWER_WRONG;
	}

	/**
	 * 文字闪烁
	 */
	private void sparkTheWords() {
		// 定时器相关
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

						// 执行闪烁逻辑：交替显示红色和白色的文字
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
	 * 自动选择一个答案
	 */
	private void tipAnswer() {
		boolean tipWord = false;
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).getmWordString().length() == 0) {
				// 根据当前的答案框条件选择对应的文字并填入
				onWordButtonClick(findIsAnswerWord(i));

				tipWord = true;

				// 减少金币
				if (!handleCoins(-getTipCoins())) {
					// 金币不够，显示提示对话框
					return;
				}
				break;
			}
		}

		// 没有找到可以填充的答案
		if (!tipWord) {
			// 闪烁文字提示用户
			sparkTheWords();
		}

	}

	/**
	 * 删除文字
	 */
	private void deleteOneWord() {
		// 减少金币
		if (!handleCoins(-getDeleteWordCoins())) {
			// 金币不够，显示提示对话框
			return;
		}

		// 将这个索引对应的wordButton设置为不可见
		setButtonVisiable(findNotAnswerWord(), View.INVISIBLE);
	}

	/**
	 * 找到一个不是答案的文件，而且当前是可见的
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
	 * 找到一个答案文字,index为索引
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
	 * 判断某个文字是否为答案
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
	 * 增加或者减少指定数量的金币
	 * 
	 * @param data
	 * @return true 增加/减少成功 false 失败
	 */
	private boolean handleCoins(int data) {
		// 判断当前总的金币数量是否可减少
		if (mCurrentCoins + data >= 0) {
			mCurrentCoins += data;

			mViewCurrentCoins.setText(mCurrentCoins + "");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 从配置文件里读取删除操作所要用的金币
	 * 
	 * @return
	 */
	private int getDeleteWordCoins() {
		return getResources().getInteger(R.integer.pay_delete_answer);
	}

	/**
	 * 从配置文件里读取提示操作所要用的金币
	 * 
	 * @return
	 */
	private int getTipCoins() {
		return getResources().getInteger(R.integer.pay_tip_answer);
	}

	/**
	 * 处理删除待选文字的事件
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
	 * 处理提示按键事件
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
