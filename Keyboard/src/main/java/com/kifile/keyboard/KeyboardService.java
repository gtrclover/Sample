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

package com.kifile.keyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class KeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private InputMethodManager mInputManager;
    private KeyboardManager mKeyboardManager;
    private KeyboardView mInputView;

    @Override
    public void onCreate() {
        super.onCreate();
        mInputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mKeyboardManager = new KeyboardManager(getApplicationContext());
    }

    @Override
    public View onCreateInputView() {
        mInputView = (KeyboardView) getLayoutInflater().inflate(R.layout.sample_latin_keyboard_view, null);
        mInputView.setOnKeyboardActionListener(this);
        return mInputView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        int type = info.inputType & InputType.TYPE_MASK_CLASS;
        Keyboard keyboard = null;
//        switch (type) {
//            case InputType.TYPE_CLASS_NUMBER:
//            case InputType.TYPE_CLASS_PHONE:
        keyboard = mKeyboardManager.getKeyboard(KeyboardManager.KeyboardType.NUMBER);
//                break;
//        }
        if (keyboard != null) {
            mInputView.setKeyboard(keyboard);
        }
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
