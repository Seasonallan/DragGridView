package com.season.drag.core;

import java.util.Stack;

import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * 拖动控制器
 * 
 * @author ziv
 * 
 */
public class DragController {

	/** 单例，确保唯一 */
	private static DragController mInstance;
	/** 缓存的监听器列表 */
	private Stack<IDragListener> mControllerList;
	/** 当前拖拽的状态 */
	private int mCurrentState;
	
	private boolean mDisableDeleteFunction = false;
	
	public void disableDelFunction(){
		this.mDisableDeleteFunction = true;
	}

	private DragController() {
		mControllerList = new Stack<IDragListener>();
		mCurrentState = IDragStatus.INVALIABLE;
	}

	public static DragController getInstance() {
		if (mInstance == null) {
			mInstance = new DragController();
		}
		return mInstance;
	}

	/**
	 * 清空单例
	 */
	public void clear() {
		mControllerList.clear();
		mInstance = null;
	}


	/**
	 * 设置当前状态为invalid
	 */
	public void changeStateReady() {
		this.mCurrentState = IDragStatus.READY;
	}

	/**
	 * 是否处于拖拽状态
	 * 
	 * @return
	 */
	public boolean isDragWorking() {
		return mCurrentState >= 0;
	}

	/**
	 * 是否准备好拖动
	 * 
	 * @return
	 */
	public boolean isDragReady() {
		return this.mDisableDeleteFunction || this.mCurrentState == IDragStatus.READY;
	}

	/**
	 * 注册拖动广播
	 * 
	 * @param listener
	 */
	public void registerDragListener(IDragListener listener) {
		if (mControllerList.contains(listener)) {
			mControllerList.remove(listener);
		}
		mControllerList.add(listener);
	}

	/**
	 * 注销拖拽广播
	 * 
	 * @param listener
	 */
	public void unRegisterDragListener(IDragListener listener) {
		mControllerList.remove(listener);
	}

	/**
	 * 开启拖拽功能
	 */
	public void notifyDragEnable() {
		mCurrentState = IDragStatus.READY;
		for (IDragListener listener : mControllerList) {
			listener.onDragEnable();
		}
	}

	/**
	 * 退出拖拽状态
	 * @return
	 */
	public boolean cancelDragMode(){
		if(isDragReady() && !mDisableDeleteFunction){
			notifyDragDisable();
			return false;
		}
		return true;
	}
	
	/**
	 * 关闭拖拽功能
	 */
	public void notifyDragDisable() {
		mCurrentState = IDragStatus.INVALIABLE;
		for (IDragListener listener : mControllerList) {
			listener.onDragDisable();
		}
	}

	/**
	 * 通知拖拽开始 【创建浮窗】
	 * 
	 * @param page
	 * @param itemView
	 * @param event
	 */
	public void notifyDragCreated(int page, ViewGroup itemView,
			MotionEvent event) {
		mCurrentState = IDragStatus.INIT;
		for (IDragListener listener : mControllerList) {
			listener.onDragViewCreate(page, itemView, event);
		}
	}

	/**
	 * 通知拖拽结束【移除浮窗】
	 * 
	 * @param page
	 * @param event
	 */
	public void notifyDragDrop(int page, MotionEvent event) {
		mCurrentState = IDragStatus.READY;
		for (IDragListener listener : mControllerList) {
			listener.onDragViewDestroy(page, event);
		}
	}

	/**
	 * 通知子视图自动移位
	 * 
	 * @param page
	 * @param event
	 */
	public void notifyDrag(int page, MotionEvent event) {
		mCurrentState = IDragStatus.DRAG;
		for (IDragListener listener : mControllerList) {
			listener.onItemMove(page, event);
		}
	}

	/**
	 * 通知页面切换【开始执行操作一:旧页面移除拖动的视图】
	 * 
	 * @param lastPage
	 * @param currentPage
	 */
	public void notifyPageSnape(int lastPage, int currentPage) {
		for (IDragListener listener : mControllerList) {
			listener.onPageChange(lastPage, currentPage);
		}
	}

	/**
	 * 通知页面切换操作一执行完毕 ,开始执行操作二【新页面替换操作一移除的视图到第一项或者最后一项】
	 * 
	 * @param lastPage
	 * @param currentPage
	 * @param object
	 */
	public <T> void notifyPageChangeRemoveDragItem(int lastPage,
			int currentPage, T object) {
		for (IDragListener listener : mControllerList) {
			listener.onPageChangeRemoveDragItem(lastPage, currentPage, object);// (lastPage,
																				// currentPage);
		}
	}

	/**
	 * 通知页面切换操作三可以开始执行【旧页面添加新页面移除的视图】
	 * 
	 * @param lastPage
	 * @param currentPage
	 * @param object
	 */
	public <T> void notifyPageSnapeReplaceFirstItem(int lastPage,
			int currentPage, T object) {
		for (IDragListener listener : mControllerList) {
			listener.onPageChangeReplaceFirstItem(lastPage, currentPage, object);
		}
	}

	/**
	 * 通知页面切换成功，解锁拖拽
	 */
	public <T> void notifyPageChangeFinish() {
		for (IDragListener listener : mControllerList) {
			listener.onPageChangeFinish();
		}
	}


	/**
	 * 通知删除某个项目
	 * @param page
	 * @param position
	 */
	public <T> void notifyDeleteItemInPage(int page, int position) {
		for (IDragListener listener : mControllerList) {
			listener.onItemDelete(page, position);
		}
	}


	/**
	 * 通知删除某个项目
	 * @param page
	 * @param position
	 */
	public <T> void notifyDeleteItemInPage(int totalPage, int page, int removePage, int position, T object) {
		for (IDragListener listener : mControllerList) {
			listener.onItemDelete(totalPage, page, removePage, position, object);
		}
	} 
}









