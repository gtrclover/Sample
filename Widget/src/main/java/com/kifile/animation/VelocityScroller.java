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

package com.kifile.animation;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by kifile on 14/10/21.
 */
public class VelocityScroller extends Scroller {
    private int mXFactory;
    private int mYFactory;
    private int mVelocityX;
    private int mVelocityY;
    private int mCurrX;
    private int mCurrY;
    private boolean mIsInVelocityMode;

    public VelocityScroller(Context context) {
        super(context);
    }

    public VelocityScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mIsInVelocityMode = false;
        super.startScroll(startX, startY, dx, dy, duration);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int velocityX, int velocityY, int duration) {
        mVelocityX = velocityX;
        mVelocityY = velocityY;
        mXFactory = (int) (duration * duration * (1f * dx / velocityX - 1f * 4 / 3 * duration + duration * duration));
        mYFactory = (int) (duration * duration * (1f * dy / velocityY - 1f * 4 / 3 * duration + duration * duration));
        mIsInVelocityMode = true;
        startScroll(startX, startY, dx, dy, duration);
    }

    @Override
    public boolean computeScrollOffset() {
        if (!mIsInVelocityMode) {
            return super.computeScrollOffset();
        } else {
            if (isFinished()) {
                return false;
            }

            int timePassed = timePassed();

            if (timePassed < getDuration()) {
                int deltaX = (int) (mVelocityX * (1f * timePassed * timePassed * timePassed / 3 + getDuration() *
                        getDuration() * timePassed * timePassed + getDuration() * getDuration() * timePassed +
                        mXFactory) / (getDuration() * getDuration()));
                int deltaY = (int) (mVelocityY * (1f * timePassed * timePassed * timePassed / 3 + getDuration() *
                        getDuration() * timePassed * timePassed + getDuration() * getDuration() * timePassed +
                        mYFactory) / (getDuration() * getDuration()));
                mCurrX = getStartX() + deltaX;
                mCurrY = getStartY() + deltaY;
            } else {
                mCurrX = getFinalX();
                mCurrY = getFinalY();
                forceFinished(true);
            }
            return true;
        }
    }

    public final int getRealX() {
        if (!mIsInVelocityMode) {
            return super.getCurrX();
        } else {
            return mCurrX;
        }
    }

    public final int getRealY() {
        if (!mIsInVelocityMode) {
            return super.getCurrY();
        } else {
            return mCurrY;
        }
    }

}
