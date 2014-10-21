package com.kifile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.*;
import android.widget.FrameLayout;
import com.kifile.animation.VelocityScroller;
import com.kifile.animation.ViscousFluidInterpolator;


/**
 * 连续的ScrollView
 */
public class SerialScrollLayout extends FrameLayout {
    public static final String TAG = "SerialScrollLayout";
    private static final int DURATION = 1000;
    private static final int SCROLL_DELAY = 3000;
    private static final int VELOCITY_FACTORY = 10;
    private static final int SCREEN_FACTORY = 5;

    private int mLastPosition;
    private boolean mIsBeingDragged;
    private boolean mAutoScroll;
    private int mScrollDuration;
    private int mScrollDelay;
    private int mMaxLoop;

    private VelocityScroller mScroller;

    private int mTouchSlop;
    private int mMaximumVelocity;
    private int mMinimumFlingVelocity;
    private VelocityTracker mVelocityTracker;
    private OnScrollChangedListener mScrollChangedListener;

    private int mCurrPage;
    private int mWidth;
    private int mHeight;
    private int mPositionOffset;

    private static final int MSG_AUTO_SCROLL = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTO_SCROLL:
                    removeMessages(msg.what);
                    if (mAutoScroll) {
                        if (mMaxLoop != 0) {
                            if (!mIsBeingDragged) {
                                moveToNextView();
                            }
                            sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mScrollDelay);
                            if (mMaxLoop > 0) {
                                mMaxLoop--;
                            }
                        }
                    }
                    break;
            }

        }
    };

    public SerialScrollLayout(Context context) {
        this(context, null);
    }

    public SerialScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialScrollLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mScroller = new VelocityScroller(getContext(), new ViscousFluidInterpolator());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SerialScrollLayout, defStyle, 0);
        mAutoScroll = a.getBoolean(R.styleable.SerialScrollLayout_autoScroll, false);
        mScrollDuration = a.getInteger(R.styleable.SerialScrollLayout_scrollDuration, DURATION);
        mScrollDelay = a.getInteger(R.styleable.SerialScrollLayout_scrollDelay, SCROLL_DELAY);
        mMaxLoop = a.getInteger(R.styleable.SerialScrollLayout_maxLoop, -1);
        a.recycle();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE && getVisibility() == VISIBLE) {
            if (mAutoScroll) {
                mHandler.removeMessages(MSG_AUTO_SCROLL);
                mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mScrollDelay);
            }
            if (mCurrPage >= getChildCount()) {
                moveToView(getChildCount() - 1);
            } else if (mCurrPage < 0) {
                moveToView(0);
            }
        } else {
            mHandler.removeMessages(MSG_AUTO_SCROLL);
        }
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int position = getPosition(ev);
                mLastPosition = position;
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mIsBeingDragged = !mScroller.isFinished();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int position = getPosition(ev);
                final int diff = Math.abs(position - mLastPosition);
                if (diff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastPosition = position;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                recycleVelocityTracker();
                if (mAutoScroll) {
                    mHandler.removeMessages(MSG_AUTO_SCROLL);
                    mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mScrollDelay);
                }
                moveToView(mCurrPage);
                break;
        }
        return mIsBeingDragged;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();

        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mIsBeingDragged = !mScroller.isFinished()) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastPosition = getPosition(event);
                break;
            case MotionEvent.ACTION_MOVE:
                final int position = getPosition(event);
                int delta = mLastPosition - position;
                if (!mIsBeingDragged && Math.abs(delta) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (delta > 0) {
                        delta -= mTouchSlop;
                    } else {
                        delta += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    mLastPosition = position;
                    scrollBy(delta, 0);
                    mVelocityTracker.clear();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity();
                    computeTouchScroll(initialVelocity);
                    recycleVelocityTracker();
                }
                if (mAutoScroll) {
                    mHandler.removeMessages(MSG_AUTO_SCROLL);
                    mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mScrollDelay);
                }
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return true;
    }

    private void computeTouchScroll(int velocity) {
        int start = mCurrPage * mWidth;
        int offset = getScrollX() - start;

        if (offset > 0) {
            //scroll to left
            if (Math.abs(velocity) > mMaximumVelocity / VELOCITY_FACTORY || Math.abs(offset) > mWidth / SCREEN_FACTORY) {
                mCurrPage++;
                mScroller.startScroll(getScrollX(), getScrollY(), mCurrPage * mWidth - getScrollX(), 0, velocity, 0, 500);
                postInvalidate();
            } else {
                moveToView(mCurrPage);
            }
        } else {
            if (Math.abs(velocity) > mMaximumVelocity / VELOCITY_FACTORY || Math.abs(offset) > mWidth / SCREEN_FACTORY) {
                mCurrPage--;
                mScroller.startScroll(getScrollX(), getScrollY(), mCurrPage * mWidth - getScrollX(), 0, velocity, 0, 500);
                postInvalidate();
            } else {
                moveToView(mCurrPage);
            }
        }
    }

    private int getPosition(MotionEvent ev) {
        return (int) ev.getX();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getRealX(), mScroller.getRealY());
            invalidate();
        } else {
            if (mCurrPage == 0) {
                onMoveToFirstView();
            } else if (mCurrPage == getChildCount() - 1) {
                onMoveToLastView();
            }
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mWidth = getMeasuredWidth();
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
        scrollTo(mCurrPage * mWidth, 0);
    }

    public void moveToView(int position) {
        if (mIsBeingDragged) {
            //we can't scroll when draged
            return;
        }
        mCurrPage = position;
        int x = mWidth * mCurrPage;
        mScroller.startScroll(getScrollX(), getScrollY(), x - getScrollX(), 0, mScrollDuration);
        postInvalidate();
    }

    public void moveToLastView() {
        moveToView(mCurrPage - 1);
    }

    public void moveToNextView() {
        moveToView(mCurrPage + 1);
    }

    private void onMoveToFirstView() {
        final View last = getChildAt(getChildCount() - 1);
        removeView(last);
        addView(last, 0);
        mCurrPage = 1;
        mPositionOffset--;
    }

    private void onMoveToLastView() {
        final View first = getChildAt(0);
        removeView(first);
        addView(first);
        mCurrPage = getChildCount() - 2;
        mPositionOffset++;
    }

    private long mLastUpdateTime;

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (mScrollChangedListener != null) {
            if (mIsBeingDragged) {
                //being dragged means user still touch the screen
                final long curr = System.currentTimeMillis();
                if (curr - mLastUpdateTime < 100) {
                    return;
                }
                mLastUpdateTime = curr;
                mPositionOffset %= getChildCount();
                final int page = mCurrPage;
                int fromPosition = (mPositionOffset + page) % getChildCount();
                if (fromPosition < 0) {
                    fromPosition += getChildCount();
                }
                final boolean toLeft = getScrollX() > page * mWidth;
                int toPosition = toLeft ? fromPosition + 1 : fromPosition - 1;
                if (toPosition < 0) {
                    toPosition += getChildCount();
                } else if (toPosition >= getChildCount()) {
                    toPosition -= getChildCount();
                }
                int toAlpha = Math.abs(getScrollX() - page * mWidth) * 255 / mWidth;
                int fromAlpha = 255 - toAlpha;
                mScrollChangedListener.onScroll(fromPosition, toPosition, fromAlpha, toAlpha);
            } else if (!mScroller.isFinished()) {
                //scroller don't finish means the scrren auto scrolling
                final long curr = System.currentTimeMillis();
                if (curr - mLastUpdateTime < 100) {
                    return;
                }
                mLastUpdateTime = curr;
                mPositionOffset %= getChildCount();
                final int page = mScroller.getStartX() / mWidth;
                int fromPosition = (mPositionOffset + page) % getChildCount();
                if (fromPosition < 0) {
                    fromPosition += getChildCount();
                }
                final boolean toLeft = mScroller.getFinalX() / mWidth > page;
                int toPosition = toLeft ? fromPosition + 1 : fromPosition - 1;
                if (toPosition < 0) {
                    toPosition += getChildCount();
                } else if (toPosition >= getChildCount()) {
                    toPosition -= getChildCount();
                }
                int toAlpha = Math.abs(getScrollX() - page * mWidth) * 255 / mWidth;
                int fromAlpha = 255 - toAlpha;
                mScrollChangedListener.onScroll(fromPosition, toPosition, fromAlpha, toAlpha);
            } else {
                //the screen is stop
                mPositionOffset %= getChildCount();
                int realPosition = (mPositionOffset + mCurrPage) % getChildCount();
                if (realPosition < 0) {
                    realPosition += getChildCount();
                }
                mScrollChangedListener.onScrollChanged(realPosition);
            }
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        this.mScrollChangedListener = listener;
    }

    public boolean getAutoScroll() {
        return mAutoScroll;
    }

    public void setAutoScroll(boolean auto) {
        if (mAutoScroll != auto) {
            if (auto) {
                mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mScrollDelay);
            } else {
                mHandler.removeMessages(MSG_AUTO_SCROLL);
            }
            this.mAutoScroll = auto;
        }
    }

    public int getScrollDelay() {
        return mScrollDelay;
    }

    public void setScrollDelay(int delay) {
        this.mScrollDelay = delay;
    }

    public int getScrollDuration() {
        return mScrollDuration;
    }

    public void setScrollDuration(int duration) {
        this.mScrollDuration = duration;
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int position);

        void onScroll(int fromPosition, int toPosition, int fromAlpah, int toAlpha);
    }
}
