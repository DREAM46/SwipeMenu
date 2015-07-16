#自定义的SwipeView解析
------
##SwipeView的布局原理
其实，SwipeView是一个继承于FrameLayout的自定义控件，而其实它的布局很简单，就是内容部分占了整个屏幕的宽，而滑动部分却因布局在屏幕之外，而有被隐藏的错觉，当整个条目向左滑时，内容部分逐渐移出屏幕而出现隐藏的效果，而滑动部分逐渐移入屏幕而出现渐入的效果。

------
##SwipeView的布局文件

<code>  
  
    <com.example.swipedelete.SwipeView
        android:id="@+id/swipeView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" >

        <include layout="@layout/layout_content" />

        <include layout="@layout/layout_delete" />
    </com.example.swipedelete.SwipeView>
</code>

##SwipeView Java文件及解析
###一些自定义View所需的方法
<code>   
	
	 /**
	 * 从xml文件中加载完布局，不可以获取内部控件的宽高，只知道自己有多少个子View，而没有对其进行测量
	 * 一般可以初始化子View的引用，不可以使用findViewById(int id)
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.contentView = this.getChildAt(0);
		this.deleteView = this.getChildAt(1);
	}

	/**
	 * 测量完子View调用,只要大小高度发生改变都可以调用，在这里可以获取子View的高度
	 * getMeasuredWidth():只要在onMeasured()执行完之后，便可以通过调用此方法获取得到宽度，推荐用这个
	 * getWidth():只有在onLayout执行完之后，才可以通过调用此方法获取得到宽度
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
	 * 内容部分占了整个屏幕的宽，而滑动部分却因布局在屏幕之外，而有被隐藏的错觉
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		contentView.layout(0, 0, contentWidth, contentHeight);
		deleteView.layout(contentWidth, 0, contentWidth + deleteWidth,
				deleteHeight);
	}


</code>

###ViewDragHelper的解析
其实ViewDragHelper并不是第一个用于分析手势处理的类，gesturedetector也是，但是在和拖动相关的手势分析方面gesturedetector只能说是勉为其难。

关于ViewDragHelper有如下几点：

ViewDragHelper.Callback是连接ViewDragHelper与view之间的桥梁（这个view一般是指拥子view的容器即parentView）；

   ViewDragHelper的实例是通过静态工厂方法创建的；

   你能够指定拖动的方向；

   ViewDragHelper可以检测到是否触及到边缘；

   ViewDragHelper并不是直接作用于要被拖动的View，而是使其控制的视图容器中的子View可以被拖动，如果要指定某个子view的行为，需要在Callback中想办法；

   ViewDragHelper的本质其实是分析onInterceptTouchEvent和onTouchEvent的MotionEvent参数，然后根据分析的结果去改变一个容器中被拖动子View的位置（ 通过offsetTopAndBottom(int offset)和offsetLeftAndRight(int offset)方法 ），他能在触摸的时候判断当前拖动的是哪个子View；

   虽然ViewDragHelper的实例方法 ViewDragHelper create(ViewGroup forParent, Callback cb) 可以指定一个被ViewDragHelper处理拖动事件的对象 ，但ViewDragHelper类的设计决定了其适用于被包含在一个自定义ViewGroup之中，而不是对任意一个布局上的视图容器使用ViewDragHelper。

------
##滑动处理,Android API提供了ViewDragHelper.OnCallBack()
###ViewDragHelper的一些较为重要的方法的解析
每次触摸都会调用这个方法以判断是否要响应这次触摸事件，只有返回true的话就响应一下的方法
<code>
	
	/**
	  * 每次触摸都会调用这个方法以判断是否要响应这次触摸事件，返回true的话就响应
	  * 
	  * @param child
	  *            子控件
	  * @param id
	  *            子控件id
	*/
		@Override
		public boolean tryCaptureView(View child, int id) {
			return child == contentView || child == deleteView;
		}

</code>
onViewCaptured(View capturedChild, int activePointerId),当被触摸时这个方法会被回调  

<code>  

	   /**
		 * 当被触摸时这个方法会被回调
		 * 
		 * @param capturedChild
   		 *            子控件
         */
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			super.onViewCaptured(capturedChild, activePointerId);
		}  
</code>
getViewHorizontalDragRange(child)，参数为子View，这个方法设置水平方向拖拽的范围
<code>  

       /**
		 * 设置水平方向拖拽的范围，返回0即可
		 */
		@Override
		public int getViewHorizontalDragRange(View child) {
			return deleteWidth;
		}  
</code>

clampViewPositionHorizontal()控制child实际上滑动多少
<code>
  
       /**
		 * 控制child实际上滑动多少
		 * 
		 * @param child
		 *            改变的子View
		 * @param left
		 *            应该被上滑动的距离
		 * @param dx
		 *            实际上滑动的距离 打个比方，滑动了10，dx == 10，而left就是你期望滑动的距离
		 * @return 想让child的Left等于多少
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == contentView) {
				// 如果在滑动ContentView且此时的，left已经小于0，说明此时时ContentView已经占据了整个屏幕还要向左滑动，此时应该给予阻止
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
</code>
onViewPositionChanged()View滑动后的回调,主要用于设置滑动后的状态，滑动对于子控件的影响
<code> 

       /**
		 * View滑动后的回调
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

			// 用contentView的left去决定它的状态
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
</code>
滑动的状态
<code>
     
    private SwipeStatus mStatus;

	/**
	 * 返回现在的状态
	 * 
	 * @return
	 */
	public SwipeStatus getCurrentSwipeStatus() {
		return mStatus;
	}

	/**
	 * SwipeView的状态
	 * 
	 * @author 温坤哲
	 * @date 2015-7-8
	 */
	enum SwipeStatus {
		/**
		 * 打开
		 */
		Open,
		/**
		 * 关闭
		 */
		Close,
		/**
		 * 滑动中
		 */
		Swiping;
	}
  
</code>
滑动监听器,包括监听器的设置，监听器的获取
<code>  

	public interface OnSwipeStatusChangeListener {
		/**
		 * 状态变成打开状态时回调此方法
		 * 
		 * @param openSwipeView
		 *            作用的SwipeView
		 */
		void onOpen(SwipeView openSwipeView);

		/**
		 * 状态变成关闭时回调此方法
		 * 
		 * @param closeSwipeView
		 *            作用的SwipeView
		 */
		void onClose(SwipeView closeSwipeView);

		/**
		 * 状态变成滑动时回调此方法
		 * 
		 * @param closeSwipeView
		 *            作用的SwipeView
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

</code>
 由于在ListView中的子项可能回出现向上侧滑动的事件，而这种向上侧滑动势必会对侧滑产生一定的影响，所以应onViewReleased()转化为只考虑左右滑动的事件

<code>  

       /**
		 * touchUp事件的回调
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

</code>

打开，关闭，快速关闭
<code>  

     /**
	 * 打开SwipeView
	 */
	public void open() {
		helper.smoothSlideViewTo(contentView, -deleteWidth, 0);
		ViewCompat.postInvalidateOnAnimation(SwipeView.this);
	}

	/**
	 * 关闭SwipeView
	 */
	public void close() {
		helper.smoothSlideViewTo(contentView, 0, 0);
		ViewCompat.postInvalidateOnAnimation(SwipeView.this);
	}

	/**
	 * 快速关闭
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
		// 返回true动画没结束，返回false，动画结束
		if (helper.continueSettling(true)) {
			// 动画没结束，则继续刷新
			ViewCompat.postInvalidateOnAnimation(SwipeView.this);
		}
	}

</code>

最后要设置将touch事件交予ViewDragHelper管理
<code>  

    /**
	 * 由ViewDragHelper来管理触摸事件
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return helper.shouldInterceptTouchEvent(ev);
	}

	int lastX, lastY;

	/**
	 * 将touch事件交予ViewDragHelper管理,然后必须返回true
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
				// 若左右互动距离大于上下滑动的距离，则对触摸事件进行拦截，不让其父控件响应
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
</code>

MainActivity.java中的ListView
<code>  

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
</code>

实现OnSwipeStatusChangeListener接口

<code>   

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
</code>




