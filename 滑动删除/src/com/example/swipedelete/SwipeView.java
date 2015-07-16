package com.example.swipedelete;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * ��ΪFrameLayout�Ѿ�ʵ����onMeasure(),ֻ��ʵ��onLayout()
 * ʹ��ViewDragHelper������װ�˶Դ���λ�ã������ٶȣ���������ļ�⣬�Լ�Scroller,��Ҫ����ָ��ʲôʱ�򻬶�����������
 * 
 * @author ������
 * @date 2015-7-7
 */
public class SwipeView extends FrameLayout {

	private View contentView;
	private View deleteView;

	/**
	 * ���ݿؼ����
	 */
	private int contentWidth;

	/**
	 * ���ݿؼ��߶�
	 */
	private int contentHeight;

	/**
	 * ɾ���ؼ��Ŀ��
	 */
	private int deleteWidth;

	/**
	 * ɾ���ؼ��ĸ߶�
	 */
	private int deleteHeight;

	private ViewDragHelper helper;

	public SwipeView(Context context) {
		super(context);
	}

	public SwipeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	/**
	 * ��ʼ������
	 */
	private void init() {
		helper = ViewDragHelper.create(this, cb);
	}

	/**
	 * ��xml�ļ��м����겼�֣������Ի�ȡ�ڲ��ؼ��Ŀ�ߣ�ֻ֪���Լ��ж��ٸ���View����û�ж�����в���
	 * һ����Գ�ʼ����View�����ã�������ʹ��findViewById(int id)
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.contentView = this.getChildAt(0);
		this.deleteView = this.getChildAt(1);
	}

	/**
	 * ��������View����,ֻҪ��С�߶ȷ����ı䶼���Ե��ã���������Ի�ȡ��View�ĸ߶�
	 * getMeasuredWidth():ֻҪ��onMeasured()ִ����֮�󣬱����ͨ�����ô˷�����ȡ�õ���ȣ��Ƽ������
	 * getWidth():ֻ����onLayoutִ����֮�󣬲ſ���ͨ�����ô˷�����ȡ�õ����
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		this.contentWidth = contentView.getMeasuredWidth();
		this.contentHeight = contentView.getMeasuredHeight();

		this.deleteWidth = deleteView.getMeasuredWidth();
		this.deleteHeight = deleteView.getMeasuredHeight();
	}

	/**
	 * 
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		contentView.layout(0, 0, contentWidth, contentHeight);
		deleteView.layout(contentWidth, 0, contentWidth + deleteWidth,
				deleteHeight);
	}

	/**
	 * ��ViewDragHelper���������¼�
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return helper.shouldInterceptTouchEvent(ev);
	}

	int lastX, lastY;

	/**
	 * ��touch�¼�����ViewDragHelper����,Ȼ����뷵��true
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = x - lastX;
			int deltaY = y - lastY;
			if (Math.abs(deltaX) > Math.abs(deltaY)) {
				// �����һ�������������»����ľ��룬��Դ����¼��������أ������丸�ؼ���Ӧ
				this.requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		lastX = x;
		lastY = y;
		helper.processTouchEvent(event);
		return true;
	}

	private ViewDragHelper.Callback cb = new Callback() {

		/**
		 * ÿ�δ��������������������ж��Ƿ�Ҫ��Ӧ��δ����¼�������true�Ļ�����Ӧ
		 * 
		 * @param child
		 *            �ӿؼ�
		 * @param id
		 *            �ӿؼ�id
		 */
		@Override
		public boolean tryCaptureView(View child, int id) {
			return child == contentView || child == deleteView;
		}

		/**
		 * ��������ʱ��������ᱻ�ص�
		 * 
		 * @param capturedChild
		 *            �ӿؼ�
		 */
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			super.onViewCaptured(capturedChild, activePointerId);
		}

		/**
		 * ��ȡˮƽ������ק�ķ�Χ������0����
		 */
		@Override
		public int getViewHorizontalDragRange(View child) {
			return deleteWidth;
		}

		/**
		 * ����childʵ���ϻ�������
		 * 
		 * @param child
		 *            �ı����View
		 * @param left
		 *            Ӧ�ñ��ϻ����ľ���
		 * @param dx
		 *            ʵ���ϻ����ľ��� ����ȷ���������10��dx == 10����left���������������ľ���
		 * @return ����child��Left���ڶ���
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == contentView) {
				if (left > 0)
					left = 0;
				if (left < -deleteWidth)
					left = -deleteWidth;
			} else if (child == deleteView) {
				int distance = contentWidth - deleteWidth;
				if (left < distance)
					left = distance;

				if (left > contentWidth)
					left = contentWidth;
			}
			return left;
		}

		/**
		 * View������Ļص�
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			if (changedView == contentView) {
				deleteView.layout(deleteView.getLeft() + dx, 0,
						deleteView.getRight() + dx, deleteView.getBottom());
			} else if (changedView == deleteView) {
				contentView.layout(contentView.getLeft() + dx, 0,
						contentView.getRight() + dx, contentView.getBottom());
			}

			// ��contentView��leftȥ��������״̬
			int mleft = contentView.getLeft();
			if (mleft == 0 && mStatus != SwipeStatus.Close) {
				mStatus = SwipeStatus.Close;
				if (onSwipeStatusChangeListener != null)
					onSwipeStatusChangeListener.onClose(SwipeView.this);
			} else if (mleft == -deleteWidth && mStatus != SwipeStatus.Open) {
				mStatus = SwipeStatus.Open;
				if (onSwipeStatusChangeListener != null)
					onSwipeStatusChangeListener.onOpen(SwipeView.this);
			} else if (mStatus != SwipeStatus.Swiping) {
				mStatus = SwipeStatus.Swiping;
				if (onSwipeStatusChangeListener != null)
					onSwipeStatusChangeListener.onSwiping(SwipeView.this);
			}
		}

		/**
		 * touchUp�Ļص�
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			if (contentView.getLeft() < -deleteWidth / 2) {
				open();
			} else {
				close();
			}
		}

	};

	/**
	 * ��SwipeView
	 */
	public void open() {
		helper.smoothSlideViewTo(contentView, -deleteWidth, 0);
		ViewCompat.postInvalidateOnAnimation(SwipeView.this);
	}

	/**
	 * �ر�SwipeView
	 */
	public void close() {
		helper.smoothSlideViewTo(contentView, 0, 0);
		ViewCompat.postInvalidateOnAnimation(SwipeView.this);
	}

	/**
	 * ���ٹر�
	 */
	public void fastClose() {
		contentView.layout(0, 0, contentWidth, contentHeight);
		deleteView.layout(contentWidth, 0, contentWidth + deleteWidth,
				deleteHeight);
		mStatus = SwipeStatus.Close;
		if (onSwipeStatusChangeListener != null)
			onSwipeStatusChangeListener.onClose(SwipeView.this);
	}

	public void computeScroll() {
		// ����true����û����������false����������
		if (helper.continueSettling(true)) {
			// ����û�����������ˢ��
			ViewCompat.postInvalidateOnAnimation(SwipeView.this);
		}
	}

	private SwipeStatus mStatus;

	/**
	 * �������ڵ�״̬
	 * 
	 * @return
	 */
	public SwipeStatus getCurrentSwipeStatus() {
		return mStatus;
	}

	/**
	 * SwipeView��״̬
	 * 
	 * @author ������
	 * @date 2015-7-8
	 */
	enum SwipeStatus {
		/**
		 * ��
		 */
		Open,
		/**
		 * �ر�
		 */
		Close,
		/**
		 * ������
		 */
		Swiping;
	}

	public interface OnSwipeStatusChangeListener {
		/**
		 * ״̬��ɴ�״̬ʱ�ص��˷���
		 * 
		 * @param openSwipeView
		 *            ���õ�SwipeView
		 */
		void onOpen(SwipeView openSwipeView);

		/**
		 * ״̬��ɹر�ʱ�ص��˷���
		 * 
		 * @param closeSwipeView
		 *            ���õ�SwipeView
		 */
		void onClose(SwipeView closeSwipeView);

		/**
		 * ״̬��ɻ���ʱ�ص��˷���
		 * 
		 * @param closeSwipeView
		 *            ���õ�SwipeView
		 */
		void onSwiping(SwipeView swipingSwipeView);
	}

	private OnSwipeStatusChangeListener onSwipeStatusChangeListener;

	public OnSwipeStatusChangeListener getOnSwipeStatusChangeListener() {
		return onSwipeStatusChangeListener;
	}

	public void setOnSwipeStatusChangeListener(
			OnSwipeStatusChangeListener onSwipeStatusChangeListener) {
		this.onSwipeStatusChangeListener = onSwipeStatusChangeListener;
	}

}
