package com.season.drag.core;

/**
 * 滑动状态
 * @author SeasonAllan
 *
 */
public interface IDragStatus {
	/** * 不可用  */
	public static int INVALIABLE = -2;
	
	/** * 长按浮动中  */
	public static int READY = -1;

	/** * 拖拽中  */
	public static int DRAG = 1; 

	/** * 拖拽中  */
	public static int INIT = 2; 
	
}
