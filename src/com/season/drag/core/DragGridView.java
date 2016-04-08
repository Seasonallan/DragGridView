package com.season.drag.core;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * 拖动Gridview
 * @author SeasonAllan
 * @param <T>
 *
 */
public class DragGridView<T> extends GridView implements IDragListener{
	public static final int DURATION = 250;

	protected int dragItem;
	private int mCurrentPage = 0;
	Context mContext; 
 
	private DragController mDragController;
	private SequeueAnimThread mAnimThread;
	private int[][] mOldAnimOffset;
 
	
	public DragGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DragGridView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 设置当前页面所属页码
	 * @param page
	 */
	public void setCurrentPageId(int page){
		this.mCurrentPage = page;
	}
	
	private void init(Context context) {
		mContext = context; 
		mDragController = DragController.getInstance();
		mDragController.registerDragListener(this);
	}
   
	public boolean setOnItemLongClickListener(final MotionEvent ev) {
		this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {System.out.println("onItemLongClick "+mDragController.isDragReady());
				if(mDragController.isDragReady()){ 
					dragItem = arg2;
					getGridAdapter().setMovingState(true);
					ViewGroup itemView = (ViewGroup) getChildAt(dragItem
							- getFirstVisiblePosition());
					startDragThread(); 
					mDragController.notifyDragCreated(mCurrentPage, itemView, ev);
					requestDisallowInterceptTouchEvent(false); 
				}else{
					mDragController.notifyDragEnable();
				}
				return true;
			};
		});
		return super.onInterceptTouchEvent(ev);
	}
	
	private void startDragThread(){
		if(mAnimThread == null){
			mOldAnimOffset = new int[getCount()][2]; 
			mAnimThread = new SequeueAnimThread(); 
			mAnimThread.startThread(dragItem);
		}
	}
	private void stopDragThread(){
		if(mAnimThread != null) {
			mAnimThread.stopThread();
		}
		mAnimThread = null;
	}
	
	
	public static int NUM_COLUMNS = 3;
	@Override
	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
		NUM_COLUMNS = numColumns;
	}



	private DragAdapter<T> adapter;
	
	public void setAdapter(DragAdapter<T> adapter){
		super.setAdapter(adapter);
		this.adapter = adapter;
	}

	private void showDropAnimation(MotionEvent event){
		getGridAdapter().setMovingState(false); 
		final int lastPosition = mAnimThread.getLastPosition() < 0 ? dragItem : mAnimThread.getLastPosition(); 
		visible();
		T oldData = adapter.getItem(dragItem);
		adapter.removeItem(dragItem);
		adapter.addItem(lastPosition, oldData);   
		stopDragThread();
	}	 

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) { 
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			return setOnItemLongClickListener(ev);
		}
		return super.onInterceptTouchEvent(ev);
	}

	protected void visible() {
		for (int i = 0; i < getCount(); i++) {
			View child = getChildAt(i);
			if(child != null){
				child.setVisibility(View.VISIBLE);
			} 
		}
	}
	
	/**
	 * 多item移动
	 * @param x
	 * @param y
	 */
	public void onItemsMove(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		int dropPosition = pointToPosition(x, y);
		if (dropPosition != AdapterView.INVALID_POSITION){
			mAnimThread.setLastPosition(dropPosition);
		} 
		postInvalidate();
	}  
 
 
	private void adapterDataSetChangedNotify(){
		getGridAdapter().notifyDataSetChanged();
		new Handler().post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void onDragViewCreate(int page, ViewGroup itemView, MotionEvent event) { 
	}

	@Override
	public void onDragViewDestroy(int page, MotionEvent event) {
		// TODO Auto-generated method stub
		if(page == mCurrentPage)
			showDropAnimation(event); 
	}

	@Override
	public void onItemMove(int page, MotionEvent event) {
		// TODO Auto-generated method stub
		if(page == mCurrentPage){
			onItemsMove(event);
		}
	}
	
	@Override
	public void onPageChange(int lastPage, int currentPage) {
		// TODO Auto-generated method stub
		if(lastPage == mCurrentPage){//添加数据到该页尾部 
			getGridAdapter().setMovingState(false); 
			stopDragThread();
			mDragController.notifyPageChangeRemoveDragItem(lastPage, currentPage, getGridAdapter().remove(dragItem));
			adapterDataSetChangedNotify();
		}else if(currentPage == mCurrentPage){//移除该页第一项到上一个页面
			getGridAdapter().setMovingState(true);  
			dragItem = (lastPage < currentPage? 0:getGridAdapter().getCount()-1); 
			getGridAdapter().resetGonePosition(lastPage < currentPage? 0:getGridAdapter().getCount()-1);
			adapterDataSetChangedNotify();
		}
	}
 
	@SuppressWarnings("hiding")
	@Override
	public <T> void onPageChangeRemoveDragItem(int lastPage, int currentPage,
			T object) {
		// TODO Auto-generated method stub
		if(lastPage == mCurrentPage){//添加数据到该页尾部
			 
		}else if(currentPage == mCurrentPage){//移除该页第一【或最后一个】项到上一个页面
			mDragController.notifyPageSnapeReplaceFirstItem(lastPage, currentPage, 
					getGridAdapter().replace((lastPage < currentPage? 0:getGridAdapter().getCount()-1), object));
			adapterDataSetChangedNotify();
		}
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> void onPageChangeReplaceFirstItem(int lastPage,
			int currentPage, T object) {
		// TODO Auto-generated method stub
		if(lastPage == mCurrentPage){//添加数据到该页尾部 
			getGridAdapter().add((lastPage < currentPage? getGridAdapter().getCount() :0), object);
			adapterDataSetChangedNotify();
			mDragController.notifyPageChangeFinish();
		}else if(currentPage == mCurrentPage){//移除该页第一项到上一个页面
			startDragThread();
		}
	}
 

	private DragAdapter<?> mGridViewAdapter;
	public DragAdapter<?> getGridAdapter(){
		if(mGridViewAdapter == null){
			mGridViewAdapter = (DragAdapter<?>) getAdapter();
		}
		return mGridViewAdapter;
	}

	@Override
	public void onPageChangeFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragEnable() {
		// TODO Auto-generated method stub
		getGridAdapter().enableDrag();
	}

	@Override
	public void onDragDisable() {
		// TODO Auto-generated method stub
		getGridAdapter().disableDrag();
	}

	@Override
	public void onItemDelete(int page, int position) {
		// TODO Auto-generated method stub 
	}
 
 
	private void onItemDeleteMove(int position){
		int MoveNum = getGridAdapter().getCount()- 1 - position;
		int dragPosition = position;
		if (MoveNum != 0) { 
			int itemMoveNum = Math.abs(MoveNum);
			for (int i = 0; i < itemMoveNum; i++) {
				int holdPosition = dragPosition + 1; 
				ViewGroup moveView = (ViewGroup) getChildAt(holdPosition);
				if(moveView == null){
					continue;
				}
				Animation animation = getMoveAnimation(moveView.getLeft(), moveView.getTop(),
						getChildAt(dragPosition).getLeft(),getChildAt(dragPosition).getTop(),moveView.getLeft(), moveView.getTop()); 
				dragPosition = holdPosition;
				moveView.startAnimation(animation);
			}
		} 
		ViewGroup itemView = (ViewGroup) getChildAt(position);
		Animation removeAnimation = getRemoveAnimation();
		removeAnimation.setAnimationListener(new Animation.AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) { 
				if(getGridAdapter().getCount() == 0){
					mDragController.notifyDeleteItemInPage(-1, 1, -1, -1 , null);
				}else{
					adapterDataSetChangedNotify();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		itemView.startAnimation(removeAnimation);
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> void onItemDelete(int totalPage, int page, int removePage,  int position, T object) {
		// TODO Auto-generated method stub
		if(totalPage >= 0){ 
			if(mCurrentPage == page){
				if(page == removePage){//当前页面 执行移位操作
					onItemDeleteMove(position);
					getGridAdapter().remove(position);
					if(object != null){
						getGridAdapter().add(getGridAdapter().getCount(), object);
					}else{
					}
				} else {// 删除页面以后的页面
					if (totalPage == page) {
						mDragController.notifyDeleteItemInPage(totalPage, page - 1,
								removePage, position, getGridAdapter().remove(0));
						if (getGridAdapter().getCount() == 0) {
							mDragController.notifyDeleteItemInPage(-1, -1, removePage, -1,null);
						}
					} else {
						mDragController.notifyDeleteItemInPage(totalPage, page - 1,
								removePage, position, getGridAdapter().remove(0));
						getGridAdapter().add(getGridAdapter().getCount(), object);
					}
					adapterDataSetChangedNotify();
				}
			}
		}
		
	} 
	


	/**
	 * 获取移动动画
	 * @param x
	 * @param y
	 * @param toX
	 * @param toY
	 * @return
	 */
	public Animation getMoveAnimation(float x, float y,float toX,float toY, float fromX, float fromY) {
		//TranslateAnimation go = new TranslateAnimation(0,toX - x, 0,toY -y);  
		TranslateAnimation go = new TranslateAnimation(x-fromX,toX - fromX, y -fromY,toY -fromY); 
		go.setFillAfter(true);
		go.setDuration(DURATION);
		go.setInterpolator(new AccelerateInterpolator());
		return go;
	}

	/**
	 * 获取移动动画
	 * @param x
	 * @param y
	 * @param toX
	 * @param toY
	 * @return
	 */
	public Animation getRemoveAnimation() {
		ScaleAnimation removeAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		removeAnimation.setFillAfter(true);
		removeAnimation.setDuration(DURATION);
		return removeAnimation;
	}
	

	private Rect mTmpRect = new Rect();
	//为了能够搜索到隐藏的项，重载
	@Override
	public int pointToPosition(int x, int y) {
        Rect frame = mTmpRect;
        
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i); 
            
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return INVALID_POSITION;
    }
	private class SequeueAnimThread extends Thread{
		
		private int mLastPosition = -1;
		private int mEmptyPosition = -1;
		
		private boolean mStop = true;
		private int[] mSync = new int[0];
		private int mDirection = 0;	//运动方向
		
		public void setLastPosition(int position) {
			if(mStop || position == mEmptyPosition) {
				return;
			}
			
			mLastPosition = position;
			mDirection = (int)Math.signum(mLastPosition - mEmptyPosition);
			synchronized (mSync) {
				mSync.notify();
			}
		}
		
		public int getLastPosition() {
			return mLastPosition;
		}
		
		public boolean isStop() {
			return mStop;
		}
		
		public void startThread(int emptyPosition) {
			mEmptyPosition = emptyPosition;
			
			if(mStop) {
				mStop = false;
				try{
					start();
				}catch(Exception ignore){
					ignore.printStackTrace();
				}
			}
		}
		
		public void stopThread() {
			mLastPosition = -1;
			mEmptyPosition = -1;
			mDirection = 0;
			mStop = true;
			synchronized (mSync) {
				mSync.notify();
			}
		}
		
		@Override
		public void run() {
			while(!mStop) { 
				
				if(mEmptyPosition != mLastPosition && mLastPosition >= 0 && mLastPosition <= getCount() -1) {
					
					final int toPosition = mEmptyPosition;
					mEmptyPosition += mDirection;
					
					if(mEmptyPosition < 0 || mEmptyPosition > getCount() - 1) {
						mDirection = -mDirection;
					}else {
						
						final int fromPosition = mEmptyPosition;
						
						post(new Runnable() {
							
							@Override
							public void run() {
								setItemMoveAnim(fromPosition, toPosition);
							}
						});
						
					}
					if(mDirection != 0) {
						continue;
					}
				}
				synchronized (mSync) {
					try {
						mSync.wait();
					}catch(InterruptedException ignore){}
				}	
			}
		}
	}

	//fromPosition 要移动的项的位置
	//toPosition 目标位置
	private void setItemMoveAnim(int fromPosition, int toPosition){
		
		if(mAnimThread == null || mAnimThread.isStop()) {
			return;
		}
		
		int oldFromPosition = fromPosition;
		
		//调整动画对象,用存储的已移动x,y计算被哪个位置占用
		if(mOldAnimOffset[fromPosition][0] != 0) {	//位置被其它项占用，计算占用的项
			if(mOldAnimOffset[fromPosition][1] != 0) {	//有换行
				fromPosition -= (mOldAnimOffset[fromPosition][1] * (mOldAnimOffset[fromPosition][0] / mOldAnimOffset[fromPosition][0]));
			}else {		//无换行
				fromPosition -= mOldAnimOffset[fromPosition][0];
			}
		}
		
		View moveView = getChildAt(fromPosition - getFirstVisiblePosition());
		if(moveView != null){
			
			int fromY = mOldAnimOffset[fromPosition][1];
			int toY = toPosition / NUM_COLUMNS - fromPosition / NUM_COLUMNS;
			int fromX = mOldAnimOffset[fromPosition][0];
			int toX = toPosition  - fromPosition + -toY * NUM_COLUMNS;
			
			mOldAnimOffset[fromPosition][0] = toX;
			mOldAnimOffset[fromPosition][1] = toY;
			
			//如果目标位置是拖动项的话
			if(toPosition == dragItem) {
				mOldAnimOffset[toPosition][0] = toX;
				mOldAnimOffset[toPosition][1] = toY;
			}else if(oldFromPosition == dragItem) {
				mOldAnimOffset[oldFromPosition][0] = 0;
				mOldAnimOffset[oldFromPosition][1] = 0;
			} 
			
			Animation anim = null; 
			
			anim = new TranslateAnimation(Animation.ABSOLUTE, fromX * mAnimDistanceX
					, Animation.ABSOLUTE, toX * mAnimDistanceX
					, Animation.ABSOLUTE, fromY * mAnimDistanceY
					, Animation.ABSOLUTE, toY * mAnimDistanceY);
			
			anim.setDuration(200);
			anim.setFillAfter(true);
			
			moveView.startAnimation(anim);
			
		}
	}

	private int mAnimDistanceX;
	private int mAnimDistanceY;
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(mAnimDistanceX <= 0) {
			mAnimDistanceX = getChildCount() > 1 ? getChildAt(1).getLeft() - getChildAt(0).getLeft() : 0;
		}
		
		if(mAnimDistanceY <= 0) {
			mAnimDistanceY = getChildCount() > NUM_COLUMNS ? getChildAt(NUM_COLUMNS).getTop() - getChildAt(0).getTop() : 0;
		}
	}

}









