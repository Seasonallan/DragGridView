package com.season.drag.core;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Scroller;
 

/**
 * 仿Launcher中的WorkSapce，可以左右滑动切换屏幕的�? * 
 * @author Yao.GUET blog: http://blog.csdn.net/Yao_GUET date: 2011-05-04
 */
public class DragScrollView extends ViewGroup implements IDragListener {
 
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private int mCurScreen;
	private int mDefaultScreen = 0;

	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;

	private static final int SNAP_VELOCITY = 600;

	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX; 

	private PageListener pageListener;
	private DragController mDragController;
	
	private MotionEvent mLongClickEvent = null;
	private PageEdgeController mCountController;
	
	private boolean mDragLock = false;
	
	public DragScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public DragScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mScroller = new Scroller(context);
		
		mCountController = new PageEdgeController(context.getResources().getDisplayMetrics().widthPixels
				, (int) (8 * context.getResources().getDisplayMetrics().density));
		mDragController = DragController.getInstance();
		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		
		DragController.getInstance().registerDragListener(this);
	}
	
	public static interface ICallback<T>{
		public DragAdapter<T> getAdapter(List<T> data);
		public int getColumnNumber();
		public int getLineNumber();
		public DragGridView<T> getItemView();
	}
	
	int numColumn;
	public <T> void setAdapter(List<T> appInfos, ICallback<T> callback){ 
		int numColumn = callback.getColumnNumber(); 
		int numLine = callback.getLineNumber(); 
		int numPage = numColumn * numLine;
		for (int i = 0; i < (appInfos.size() - 1)/numPage + 1; i++) {
			DragGridView<T> gridView = callback.getItemView();
			if(gridView == null){
				gridView = new DragGridView<T>(getContext());
			}
			List<T> datas = appInfos.subList(i*numPage, ((i+1)*numPage > appInfos.size()) ? appInfos.size():((i+1)*numPage));
			List<T> d = new ArrayList<T>();
			for (int j = 0; j < datas.size(); j++) {
				d.add(datas.get(j));
			} 
			DragAdapter<T> adapter1 = callback.getAdapter(d);
			adapter1.setCurrentPage(i); 
			gridView.setNumColumns(numColumn);
			gridView.setCurrentPageId(i); 
			gridView.setAdapter(adapter1); 
			addView(gridView);
		}
	} 
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");
		}

		/**
		 * wrap_content 传进去的是AT_MOST 固定数�?或fill_parent 传入的模式是EXACTLY
		 */
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
		}

		// The children are given the same width and height as the scrollLayout
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen * width, 0);
	}

	/**
	 * According to the position of current layout scroll to the destination
	 * page.
	 */
	public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	public void snapToScreen(int whichScreen) {
		// get the valid layout page
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth())) {
			
			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
			mCurScreen = whichScreen;
			if(pageListener != null){
				pageListener.page(mCurScreen);
			}
			invalidate(); // Redraw the layout
		}
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
	}

	/**
	 * 获得当前页码
	 */
	public int getCurScreen() {
		return mCurScreen;
	}
 
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
 
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getRawX();
		final float y = event.getRawY();
 
		switch (action) { 
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (dragImageView != null) { 
				mLongClickEvent = null;
				if(!mDragLock){
					mDragController.notifyDrag(getCurScreen(), event);
					onDrag((int)x, (int)y); 
				}
			}else{
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				if(deltaX <=0 ){
					if(getScrollX() >0 ){
						scrollBy(deltaX, 0);
					}
				}else{
					if(getScrollX() < (getChildCount()- 1)  * getWidth()){
						scrollBy(deltaX, 0);
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: 
			if(mDragController.isDragWorking())
				mDragController.notifyDragDrop(getCurScreen(), event);
			if (dragImageView != null) {
				if (dragImageView != null) {
					dragImageView.setAlpha(60);
					windowManager.removeView(dragImageView);
					dragImageView = null;
				}
			}else{
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
					// Fling enough to move left
					snapToScreen(mCurScreen - 1);
					
				} else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
					// Fling enough to move right
					snapToScreen(mCurScreen + 1);
				} else {
					snapToDestination();
				}
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			break; 
		}
		return true;
	}
 
	@SuppressWarnings("deprecation")
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) { 
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
 
		final float x = ev.getRawX(); 

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			if (xDiff > mTouchSlop || dragImageView != null) { 
				mTouchState = TOUCH_STATE_SCROLLING;
			} 
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			if (dragImageView != null && mLongClickEvent != null) {
				mDragController.notifyDragDrop(getCurScreen(), mLongClickEvent);
				dragImageView.setAlpha(60);
				windowManager.removeView(dragImageView);
				dragImageView = null;
			}else{
				mDragController.changeStateReady();
			}
			break;
		} 
		return mTouchState != TOUCH_STATE_REST;
	}

	public void setPageListener(PageListener pageListener) {
		this.pageListener = pageListener;
	}

	public interface PageListener {
		void page(int page);
	}
  
	private int mLongClickX,mLongClickY; 

	private int dragStartPointY;
	private int dragStartPointX;
 
	private int dragOffsetY;
	private int dragOffsetX;
 
	/**
	 * 生成图片过程
	 * @param itemView
	 * @param bitmap
	 */
	private void showCreateDragImageAnimation(final ViewGroup itemView){ 
		itemView.destroyDrawingCache();
		itemView.setDrawingCacheEnabled(true);
		itemView.setDrawingCacheBackgroundColor(0x00000000);
		Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache(true));
		Bitmap orignalBitmp = Bitmap.createBitmap(bm, 0,0, bm.getWidth(), bm.getHeight());
		final Bitmap bitmap = scaleBitmpa(orignalBitmp, 1.0f); 
		
		if(mDragController.isDragWorking()){
			createBitmapInWindow(bitmap, mLongClickX, mLongClickY);
			itemView.setVisibility(View.GONE); 
		} 
	}

	private ImageView dragImageView = null;
	private WindowManager windowManager = null;
	private WindowManager.LayoutParams windowParams = null;
	/**
	 * 创建图片
	 * @param bm
	 * @param x
	 * @param y
	 */
	private void createBitmapInWindow(Bitmap bm, int x, int y) { 
		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP | Gravity.LEFT; 
		windowParams.x = x + dragOffsetX - dragStartPointX;
		windowParams.y = y + dragOffsetY - dragStartPointY;
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.format= PixelFormat.RGBA_8888; //设置图片格式，效果为背景透明
		windowParams.alpha = 0.8f;
		ImageView iv = new ImageView(getContext());
		iv.setImageBitmap(bm);
		 
		windowManager = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		if (dragImageView != null) {
			windowManager.removeView(dragImageView);
		}
		windowManager.addView(iv, windowParams);
		dragImageView = iv;
	} 


	/**
	 * 拖动图片
	 * @param x
	 * @param y
	 */
	private void onDrag(int x, int y) {
		if (dragImageView != null) { 
			mCountController.addCount(x);
			if(mCountController.isAllow2Snap2Next()){
				if (mCurScreen >= getChildCount() -1) {
					mCurScreen = getChildCount() -1;
				}else{ 
					mCurScreen ++;
					snapToScreen(mCurScreen);
					mDragLock = true;
					mDragController.notifyPageSnape(mCurScreen-1, mCurScreen);
				}
			}else if(mCountController.isAllow2Snap2Last()){
				if(mCurScreen <= 0){
					mCurScreen = 0;
				}else{ 
					mCurScreen --;
					snapToScreen(mCurScreen);
					mDragLock = true;
					mDragController.notifyPageSnape(mCurScreen+1, mCurScreen);
				}
			}
			/*y = Math.max(dragStartPointY, y);
			y = Math.min(y, getHeight() - ( - dragStartPointY));
			
			x = Math.max(dragStartPointX, x);
			x = Math.min(x, getWidth() - (halfItemWidth*2 - dragStartPointX));*/
			
			int[] location = new int[2];
			getLocationOnScreen(location);
			
			windowParams.alpha = 0.8f;
			windowParams.x = x + location[0] - dragStartPointX;
			windowParams.y = y + location[1] - dragStartPointY;
 
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
	}
	
	@Override
	public void onDragViewCreate(int page, ViewGroup itemView , MotionEvent event) {
		// TODO Auto-generated method stub 
		mLongClickEvent = event;
		mLongClickEvent.setAction(MotionEvent.ACTION_DOWN);
		int x = (int)event.getRawX();
		int y = (int)event.getRawY();
		dragStartPointY = y - itemView.getTop();
		dragStartPointX = x - itemView.getLeft();

		int[] location = new int[2];
		getLocationOnScreen(location);
		dragOffsetY = location[1];
		dragOffsetX = location[0];
		
		mLongClickX = (int) event.getRawX();
		mLongClickY = (int) event.getRawY();
		showCreateDragImageAnimation(itemView);
	}

	@Override
	public void onDragViewDestroy(int page, MotionEvent event) {
		// TODO Auto-generated method stub 
		mLongClickEvent = null;
	}

	@Override
	public void onItemMove(int page, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageChange(int lastPage, int currentPage) {
		// TODO Auto-generated method stub
	}

	@Override
	public <T> void onPageChangeRemoveDragItem(int lastPage, int currentPage,
			T object) {
		// TODO Auto-generated method stub
		
	}
 
	@Override
	public <T> void onPageChangeReplaceFirstItem(int lastPage,
			int currentPage, T object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageChangeFinish() {
		// TODO Auto-generated method stub
		mDragLock = false;
	}

	@Override
	public void onDragEnable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemDelete(int page, int position) {
		// TODO Auto-generated method stub
		mDragController.notifyDeleteItemInPage(getChildCount() -1, getChildCount() -1, page, position, null);
	}

	@Override
	public <T> void onItemDelete(int totalPage, int page, int removePage,  int position, T object) {
		// TODO Auto-generated method stub
		if(totalPage < 0){
			if(page > 0){
				if(getChildCount() > 1)
					snapToScreen(getChildCount() -2);
			}
			removeViewAt(getChildCount() -1);
		}
	}
	 
	/**
	 * 图片放大缩小
	 * @param bitmap
	 * @param scale
	 * @return
	 */
	public static Bitmap scaleBitmpa(Bitmap bitmap, float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
 
}


