/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.season.drag;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;

public class DragView extends View {
    private Bitmap mBitmap;
    private Paint mPaint;
    private int mRegistrationX;
    private int mRegistrationY;

    private Point mDragVisualizeOffset = null;
    private Rect mDragRegion = null; 
    private boolean mHasDrawn = false;

    ValueAnimator mAnim;
    private float mOffsetX = 0.0f;
    private float mOffsetY = 0.0f;

    private WindowManager.LayoutParams mLayoutParams;
	private WindowManager windowManager = null;

    /**
     * Construct the drag view.
     * <p>
     * The registration point is the point inside our view that the touch events should
     * be centered upon.
     *
     * @param launcher The Launcher instance
     * @param bitmap The view that we're dragging around.  We scale it up when we draw it.
     * @param registrationX The x coordinate of the registration point.
     * @param registrationY The y coordinate of the registration point.
     */
    public DragView(Context context, Bitmap bitmap, int registrationX, int registrationY,
            int left, int top, int width, int height) {
        super(context); 

		windowManager = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
        final int dragScale = 0;

        Matrix scale = new Matrix();
        final float scaleFactor = (width + dragScale) / width;
        if (scaleFactor != 1.0f) {
            scale.setScale(scaleFactor, scaleFactor);
        }

        final int offsetX = 0;
        final int offsetY = 0;
 
        // Animate the view into the correct position
        mAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
        mAnim.setDuration(110);
        mAnim.setInterpolator(new DecelerateInterpolator(2.5f));
        mAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();

                final int deltaX = (int) ((value * offsetX) - mOffsetX);
                final int deltaY = (int) ((value * offsetY) - mOffsetY);

                mOffsetX += deltaX;
                mOffsetY += deltaY;

                if (getParent() == null) {
                    animation.cancel();
                } else { 
                    mLayoutParams.x += deltaX;
                    mLayoutParams.y += deltaY; 
        			windowManager.updateViewLayout(DragView.this, mLayoutParams);
                }
            }
        });

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, scale, true);
        setDragRegion(new Rect(0, 0, width, height));

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX;
        mRegistrationY = registrationY;

        // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
        int ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        measure(ms, ms);
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public int getDragRegionLeft() {
        return mDragRegion.left;
    }

    public int getDragRegionTop() {
        return mDragRegion.top;
    }

    public int getDragRegionWidth() {
        return mDragRegion.width();
    }

    public int getDragRegionHeight() {
        return mDragRegion.height();
    }

    public void setDragVisualizeOffset(Point p) {
        mDragVisualizeOffset = p;
    }

    public Point getDragVisualizeOffset() {
        return mDragVisualizeOffset;
    }

    public void setDragRegion(Rect r) {
        mDragRegion = r;
    }

    public Rect getDragRegion() {
        return mDragRegion;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mHasDrawn = true;
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
        invalidate();
    }

    public boolean hasDrawn() {
        return mHasDrawn;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setAlpha((int) (255 * alpha));
        invalidate();
    }

    /**
     * Create a window containing this view and show it.
     *
     * @param windowToken obtained from v.getWindowToken() from one of your views
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    public void show(int touchX, int touchY) {
    	mLayoutParams = new WindowManager.LayoutParams(); 
        mLayoutParams.width = mBitmap.getWidth();
        mLayoutParams.height = mBitmap.getHeight();
        mLayoutParams.x = touchX - mRegistrationX;
        mLayoutParams.y = touchY - mRegistrationY;
  
        setLayoutParams(mLayoutParams);

		windowManager.addView(this, mLayoutParams);
        mAnim.start();
    }

    /**
     * Move the window containing this view.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    void move(int touchX, int touchY) { 
        mLayoutParams.x = touchX - mRegistrationX + (int) mOffsetX;
        mLayoutParams.y = touchY - mRegistrationY + (int) mOffsetY;
		windowManager.updateViewLayout(DragView.this, mLayoutParams);
    }

    void remove() {
        post(new Runnable() {
            public void run() {
            	windowManager.removeView(DragView.this);
            }
        });
    }

    int[] getPosition(int[] result) {
        LayoutParams lp = mLayoutParams;
        if (result == null) result = new int[2];
        result[0] = lp.x;
        result[1] = lp.y;
        return result;
    }
}

