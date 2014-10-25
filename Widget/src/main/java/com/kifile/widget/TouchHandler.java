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

import android.util.Log;
import android.view.*;

/**
 * Created by kifile on 14/10/22.
 */
public class TouchHandler {
    public static final String TAG = "TouchHandler";
    private static final boolean DBG = true;

    private void debug(String msg) {
        if (DBG) {
            Log.i(TAG, msg);
        }
    }

    private static final int NO_BOUNDARY = -1;
    private int mLeftBoundary;
    private int mRightBoundary;

    private View mView;
    private Callback mCallback;

    private boolean mIsBeingDragged = false;

    /**
     * Position of the last motion event.
     */
    private float mLastMotionX;
    private float mLastMotionY;
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mTouchSlop;

    public TouchHandler(View view) {
        this(view, NO_BOUNDARY, NO_BOUNDARY);
    }

    public TouchHandler(View view, int leftBoundary, int rightBoundary) {
        this.mView = view;
        this.mLeftBoundary = leftBoundary;
        this.mRightBoundary = rightBoundary;

        final ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public boolean handleInterceptTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mActivePointerId = event.getPointerId(0);
                initOrResetVelocityTracker();
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                final int pointerIndex = event.findPointerIndex(activePointerId);
                final float x = event.getX(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                if (xDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastMotionX = x;
                    requestParentDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onSecondaryPointerUp(event);
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return mIsBeingDragged;
    }

    public boolean handleTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mCallback != null) {
                    mCallback.onTouch();
                }
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    break;
                }
                final float x = event.getX(pointerIndex);
                float deltaX = mLastMotionX - x;
                final float y = event.getY(pointerIndex);
                float deltaY = mLastMotionY - y;
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    requestParentDisallowInterceptTouchEvent(true);
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionX = x;
                    mLastMotionY = y;
                    mView.scrollBy((int) deltaX, 0);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
                int initialVelocityY = (int) velocityTracker.getYVelocity(mActivePointerId);
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                recycleVelocityTracker();
                if (mCallback != null) {
                    mCallback.onRelease(initialVelocityX, initialVelocityY, false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                recycleVelocityTracker();
                if (mCallback != null) {
                    mCallback.onRelease(0, 0, true);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getActionIndex();
                final float x = event.getX(index);
                final float y = event.getY(index);
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = event.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                if (mCallback != null) {
                    mCallback.onPointerTouch();
                }
                onSecondaryPointerUp(event);
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent event) {
        final int pointerIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = event.getX(newPointerIndex);
            mLastMotionY = event.getY(newPointerIndex);
            mActivePointerId = event.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = mView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    public boolean isBeingDragged() {
        return mIsBeingDragged;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public interface Callback {

        void onTouch();

        void onPointerTouch();

        void onRelease(int velocityX, int velocityY, boolean cancel);
    }
}
