package com.xu.musicdemo.myui;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.xu.musicdemo.R;
import com.xu.musicdemo.model.IWordButtonClickListener;
import com.xu.musicdemo.model.WordButton;
import com.xu.musicdemo.util.Util;

public class MyGridView extends GridView {

	public final static int COUNTS_WORDS = 24;

	private ArrayList<WordButton> mArrayList = new ArrayList<WordButton>();

	private MyGridAdapter mAdapter;

	private Context mContext;

	private Animation mScaleAnimation;

	private IWordButtonClickListener mWordButtonClickListener;

	public MyGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAdapter = new MyGridAdapter();
		mContext = context;
		this.setAdapter(mAdapter);
	}

	public void updateData(ArrayList<WordButton> list) {
		mArrayList = list;

		// ������������Դ
		setAdapter(mAdapter);
	}

	class MyGridAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mArrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return mArrayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final WordButton holder;
			if (convertView == null) {
				convertView = Util.getView(mContext,
						R.layout.self_ui_gridview_item);
				holder = mArrayList.get(position);

				// ���ض���
				mScaleAnimation = AnimationUtils.loadAnimation(mContext,
						R.anim.scale);
				// ���ö����ӳ�ʱ��
				mScaleAnimation.setStartOffset(position * 100);

				holder.setmIndex(position);
				if (holder.mViewButton == null) {
					holder.mViewButton = (Button) convertView
							.findViewById(R.id.item_btn);

					holder.mViewButton
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									mWordButtonClickListener
											.onWordButtonClick(holder);
								}
							});
				}

				convertView.setTag(holder);
			} else {
				holder = (WordButton) convertView.getTag();
			}

			holder.mViewButton.setText(holder.getmWordString());

			// ���Ŷ���
			convertView.startAnimation(mScaleAnimation);

			return convertView;
		}

	}

	/**
	 * ע������ӿ�
	 * 
	 * @param iWordButtonClickListener
	 */
	public void setOnWordButtonClickListener(
			IWordButtonClickListener iWordButtonClickListener) {
		mWordButtonClickListener = iWordButtonClickListener;
	}

}
