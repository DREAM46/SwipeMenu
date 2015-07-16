package com.example.swipedelete;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swipedelete.SwipeView.OnSwipeStatusChangeListener;
import com.example.swipedelete.SwipeView.SwipeStatus;

public class MainActivity extends Activity implements OnClickListener,
		OnSwipeStatusChangeListener, OnScrollListener {

	private List<MyBean> beans;
	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		beans = new ArrayList<MyBean>();

		for (int i = 0; i < 100; i++) {
			beans.add(new MyBean(i));
		}

		ListView lv = (ListView) this.findViewById(R.id.lv);
		adapter = new MyAdapter();
		lv.setAdapter(adapter);
		lv.setOnScrollListener(this);
	}

	private class MyAdapter extends BaseAdapter {

		class ViewHolder {
			SwipeView swipeView;
			TextView tv_content;
			TextView tv_delete;
		}

		private ViewHolder holder;

		@Override
		public int getCount() {
			return beans.size();
		}

		@Override
		public Object getItem(int position) {
			return beans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return beans.get(position).getNumber();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(MainActivity.this,
						R.layout.view_list_cell, null);
				holder = new ViewHolder();
				holder.tv_delete = (TextView) convertView
						.findViewById(R.id.tv_delete);
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.tv_content);
				holder.swipeView = (SwipeView) convertView
						.findViewById(R.id.swipeView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.swipeView.fastClose();
			holder.tv_content.setText(beans.get(position).getNumber() + "");
			holder.tv_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					beans.remove(position);
					notifyDataSetChanged();
				}
			});
			holder.swipeView.setOnSwipeStatusChangeListener(MainActivity.this);
			holder.tv_content.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (unClosedSwipeViews.size() > 0)
						closeAllSwipeViews();
					Toast.makeText(MainActivity.this, position + "", 1).show();
				}

			});
			return convertView;
		}
	}

	/**
	 * 未关闭的SwipeView的集合
	 */
	private List<SwipeView> unClosedSwipeViews = new ArrayList<SwipeView>();

	@Override
	public void onOpen(SwipeView openSwipeView) {

		for (int i = 0; i < unClosedSwipeViews.size(); i++) {
			SwipeView sv = unClosedSwipeViews.get(i);
			if (sv != openSwipeView)
				sv.close();
		}

		if (!unClosedSwipeViews.contains(openSwipeView))
			unClosedSwipeViews.add(openSwipeView);
	}

	@Override
	public void onClose(SwipeView closeSwipeView) {
		unClosedSwipeViews.remove(closeSwipeView);
	}

	@Override
	public void onSwiping(SwipeView swipingSwipeView) {
		if (!unClosedSwipeViews.contains(swipingSwipeView)) {
			closeAllSwipeViews();
		}
		unClosedSwipeViews.add(swipingSwipeView);

	}

	/**
	 * 关闭所有SwipeView
	 */
	private void closeAllSwipeViews() {
		for (int i = 0; i < unClosedSwipeViews.size(); i++) {
			SwipeView sv = unClosedSwipeViews.get(i);
			if (sv.getCurrentSwipeStatus() != SwipeStatus.Close)
				sv.close();
		}
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			if (unClosedSwipeViews.size() > 0)
				closeAllSwipeViews();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

}
