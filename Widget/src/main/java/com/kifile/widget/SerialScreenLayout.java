/*
 * Copyright (C) 2014 Kifile(kifile@kifile.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kifile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import com.kifile.lib.TouchHandler;


/**
 * TODO: document your custom view class.
 */
public class SerialScreenLayout extends FrameLayout implements TouchHandler.Callback {
    public static final String TAG = "SerialScreenLayout";
    private static final boolean DBG = true;

    private int mCurrPosition;

    private void say(String msg) {
        if (DBG) {
            Log.e(TAG, msg);
        }
    }

    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    private static final int DEFAULT_GUTTER_SIZE = 16; // dips
    private static final int MIN_FLING_VELOCITY = 400; // dips

    private OnScrollChangedListener mScrollChangedListener;

    private TouchHandler mTouchHandler;
    private Scroller mScroller;

    private int mMinimumVelocity;
    private int mFlingDistance;
    private int mDefaultGutterSize;
    private int mGutterSize;

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private int mWidth;

    public SerialScreenLayout(Context context) {
        this(context, null);
    }

    public SerialScreenLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialScreenLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        mTouchHandler = new TouchHandler(this);
        mTouchHandler.setCallback(this);
        mScroller = new Scroller(getContext(), sInterpolator);
        final float density = getResources().getDisplayMetrics().density;
        mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
        mDefaultGutterSize = (int) (DEFAULT_GUTTER_SIZE * density);
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SerialScreenLayout, defStyle, 0);
        a.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mTouchHandler.handleInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTouchHandler.handleTouchEvent(event);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        this.mScrollChangedListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        final int maxGutterSize = mWidth / 10;
        mGutterSize = Math.min(maxGutterSize, mDefaultGutterSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int left = l + getPaddingLeft();
        int right = r - l - getPaddingRight();
        int top = t + getPaddingTop();
        int bottom = b - t - getPaddingTop();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();
                int childLeft;
                int childTop;
                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }

                switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = left + (right - left - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = right - width - lp.rightMargin;
                        break;
                    case Gravity.LEFT:
                    default:
                        childLeft = left + lp.leftMargin;
                }

                switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                    case Gravity.TOP:
                        childTop = top + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = top + (bottom - top - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = bottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = top + lp.topMargin;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                left += mWidth;
                right += mWidth;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        canvas.translate(mWidth * getChildCount(), 0);
        super.dispatchDraw(canvas);
        canvas.restore();
        canvas.save();
        canvas.translate(-mWidth * getChildCount(), 0);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public void onTouch() {
        mScroller.abortAnimation();
    }

    @Override
    public void onRelease(int velocityX, int velocityY, boolean cancel) {
        say("release touch,velocityX:" + velocityX);
        int targetPosition = mCurrPosition;
        if (!cancel) {
            int startX = mWidth * mCurrPosition;
            int deltaX = getScrollX() - startX;
            say(String.valueOf(mFlingDistance));
            if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocityX) > mMinimumVelocity) {
                targetPosition = deltaX > 0 ? mCurrPosition + 1 : mCurrPosition - 1;
            }
        }
        scrollToPosition(targetPosition, velocityX);
    }

    private void scrollToPosition(int targetPosition, int velocityX) {
        say("scroll to " + targetPosition);
        int startX = getScrollX();
        int finalX = targetPosition * mWidth;
        velocityX = Math.abs(velocityX);

        int duration = 500;
        if (velocityX > mMinimumVelocity) {
            final int width = mWidth;
            final int halfWidth = width / 2;
            final float distanceRatio = Math.min(1f, 1.0f * Math.abs(finalX - startX) / width);
            final float distance = halfWidth + halfWidth *
                    distanceInfluenceForSnapDuration(distanceRatio);
            duration = 4 * Math.round(1000 * Math.abs(distance / velocityX));
        }
        say("duration:" + duration);
        mScroller.startScroll(startX, 0, finalX - startX, 0, duration);
        postInvalidate();
    }

    // We want the duration of the page snap animation to be influenced by the distance that
    // the screen has to travel, however, we don't want this duration to be effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect that the distance
    // of travel has on the overall snap duration.
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    private long mLastUpdateTime;

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mCurrPosition = getScrollX() % mWidth;
        while (mCurrPosition < 0) {
            mCurrPosition += getChildCount();
        }
        while (mCurrPosition >= getChildCount()) {
            mCurrPosition -= getChildCount();
        }
        if (mScrollChangedListener != null) {
            if (mTouchHandler.isBeingDragged() || !mScroller.isFinished()) {
                //being dragged means user still touch the screen
                final long curr = System.currentTimeMillis();
                if (curr - mLastUpdateTime < 100) {
                    return;
                }
                mLastUpdateTime = curr;
                final boolean toLeft = mCurrPosition * mWidth < getScrollX();
                int fromPosition = mCurrPosition;
                int toPosition = toLeft ? mCurrPosition + 1 : mCurrPosition - 1;
                int toAlpha = Math.abs(getScrollX() - mCurrPosition * mWidth) * 255 / mWidth;
                int fromAlpha = 255 - toAlpha;
                mScrollChangedListener.onScroll(fromPosition, toPosition, fromAlpha, toAlpha);
            } else {
                //the screen is stop
                mScrollChangedListener.onScrollChanged(mCurrPosition);
            }
        }
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int position);

        void onScroll(int fromPosition, int toPosition, int fromAlpah, int toAlpha);
    }
}
