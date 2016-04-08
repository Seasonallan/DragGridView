package com.season.drag.core;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 所有隔离带view的视图适配器
 * 
 * @author ziv
 * 
 * @param <T>
 */
public abstract class DragAdapter<T> extends BaseAdapter {
 
	protected Context context;
	protected List<T> lstDate;
	public int gonePosition;
	private boolean isMoving;
	protected int rows;
	protected int page;
	protected boolean mDraging = false; 
		
	public DragAdapter(Context mContext, List<T> list) {
		this.context = mContext;
		lstDate = list;
		rows = (lstDate.size() - 1) / DragGridView.NUM_COLUMNS + 1;
	}
	
	@Override
	public int getCount() {
		return lstDate.size();
	}

	@Override
	public T getItem(int position) {
		return lstDate.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * 设置当前页码
	 * @param page
	 */
	public void setCurrentPage(int page){
		this.page = page;
	}

	/**
	 * 删除某个项
	 * @param position
	 */
	protected void deleteItemInPage(int position) {
		DragController.getInstance().notifyDeleteItemInPage(page, position);
	}
	
	/**
	 * 交换移动项值
	 * 
	 * @param startPosition
	 * @param endPosition
	 * @param gonePosition
	 */
	public void exchange(int startPosition, int endPosition, int gonePosition) {
		this.gonePosition = gonePosition;
		T startObject = getItem(startPosition);
		if (startPosition < endPosition) {
			lstDate.add(endPosition + 1, startObject);
			lstDate.remove(startPosition);
		} else {
			lstDate.add(endPosition, startObject);
			lstDate.remove(startPosition + 1);
		}
	}

	/**
	 * 移除某项
	 * 
	 * @param position
	 * @return
	 */
	public T remove(int position) {
		if(position >= getCount()){
			return null;
		}
		T object = lstDate.get(position);
		this.lstDate.remove(position);
		return object;
	}

	/**
	 * 添加某项到某个位置
	 * 
	 * @param position
	 * @param object
	 */
	@SuppressWarnings("unchecked")
	public void add(int position, Object object) {
		if (position > getCount()) {
			position = getCount();
		}
		if (position < 0) {
			position = 0;
		}
		this.lstDate.add(position, (T) object);
		notifyDataSetChanged();
	}

	/**
	 * 替换某个位置的项
	 * 
	 * @param position
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T replace(int position, Object object) {
		T res = this.lstDate.remove(position);
		lstDate.add(position, (T) object);
		return res;
	}

	/**
	 * 设置是否处于拖动的区域
	 * 
	 * @param isMoving
	 */
	public void setMovingState(boolean isMoving) {
		this.isMoving = isMoving;
	}

	/**
	 * 重置隐藏项目
	 * 
	 * @param position
	 */
	public void resetGonePosition(int position) {
		this.gonePosition = position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = getView(position);
		if (isMoving && position == gonePosition) {
			convertView.setVisibility(View.INVISIBLE);
		}else{
			convertView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}
 
	public void addItem(int position, T data) { 
		if(lstDate == null) {
			return;
		}
		
		position = Math.min(lstDate.size(), position);
		lstDate.add(position, data);
		notifyDataSetChanged();
	}
 
	public Object removeItem(int position) {
		return lstDate == null ? null : lstDate.remove(position);
	}
 
	public boolean isItemEnableDrag(int position) { 
		if(lstDate == null || position >= lstDate.size()) {
			return false;
		} 
		return true;
	}

	/**
	 * 开启拖拽功能
	 */
	public void enableDrag() {
		// TODO Auto-generated method stub
		mDraging = true; 
		notifyDataSetChanged();
	}

	/**
	 * 关闭拖拽功能
	 */
	public void disableDrag() {
		// TODO Auto-generated method stub
		mDraging = false;
		notifyDataSetChanged();
	}
	
	/**
	 * 初始化适配器视图
	 * 
	 * @param position
	 * @return
	 */
	public abstract View getView(int position); 

}
