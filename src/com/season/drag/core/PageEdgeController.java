package com.season.drag.core;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class PageEdgeController {
	private int SLEEP_TIME = 1000;
	private int COUNT_SIZE = 4;

	private boolean isSleeping;
	private int mFullWidth, mEdgeMargin;

	private List<Integer> mCountRecord;

	public PageEdgeController(int fullWidth, int margin) {
		// TODO Auto-generated constructor stub
		this.mCountRecord = new ArrayList<Integer>();
		this.isSleeping = false;
		this.mFullWidth = fullWidth;
		this.mEdgeMargin = margin;

	}

	Handler delayHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			isSleeping = false;
		}

	};

	public void addCount(int x) {
		if (x < mFullWidth - mEdgeMargin || x > mEdgeMargin) {
			wakeUp();
		}
		if (isSleeping) {
			mCountRecord = new ArrayList<Integer>();
			return;
		}
		mCountRecord.add(x);
		if (mCountRecord.size() > COUNT_SIZE) {
			mCountRecord.remove(0);
		}
	}

	@SuppressLint("HandlerLeak")
	public boolean isAllow2Snap2Next() {
		synchronized (mCountRecord) {
			if (mCountRecord.size() < COUNT_SIZE) {
				return false;
			}
			for (int i = 0; i < mCountRecord.size(); i++) {
				if (mCountRecord.get(i) < mFullWidth - mEdgeMargin) {
					return false;
				}
			}
			isSleeping = true;
			mCountRecord.clear();
			delayHandler.sendEmptyMessageDelayed(0, SLEEP_TIME);
			return true;
		}
	}

	public boolean isAllow2Snap2Last() {
		synchronized (mCountRecord) {
			if (mCountRecord.size() < COUNT_SIZE) {
				return false;
			}
			for (int i = 0; i < mCountRecord.size(); i++) {
				if (mCountRecord.get(i) > mEdgeMargin) {
					return false;
				}
			}
			isSleeping = true;
			mCountRecord.clear();
			delayHandler.sendEmptyMessageDelayed(0, SLEEP_TIME);
			return true;
		}
	}

	private void wakeUp() {
		// log("wakeUp");
		// delayHandler.removeMessages(0);
		// delayHandler.sendEmptyMessage(0);
	}

}
