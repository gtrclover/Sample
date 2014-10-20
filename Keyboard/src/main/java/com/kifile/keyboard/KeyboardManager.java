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

import android.content.Context;
import android.inputmethodservice.Keyboard;
import com.kifile.keyboard.ui.LatinKeyboard;

import java.util.WeakHashMap;

/**
 * Created by kifile on 14/10/20.
 */
public class KeyboardManager {
    private Context mContext;
    private WeakHashMap<KeyboardType, Keyboard> mKeyboardMap = new WeakHashMap<KeyboardType, Keyboard>();
    private KeyboardType mKeyboardType;

    public KeyboardManager(Context context) {
        this.mContext = context;
    }

    public Keyboard getKeyboard(KeyboardType type) {
        if (type == mKeyboardType) {
            return null;
        }
        Keyboard keyboard = mKeyboardMap.get(type);
        if (keyboard == null) {
            keyboard = new LatinKeyboard(mContext, type.getLayoutId());
            mKeyboardMap.put(type, keyboard);
        }
        mKeyboardType = type;
        return keyboard;
    }

    enum KeyboardType {
        NUMBER, TEXT, NET;

        public int getLayoutId() {
            switch (this) {
                case NUMBER:
                    return R.xml.number;
                case TEXT:
                    return 0;
                case NET:
                    return 0;
                default:
                    return 0;
            }
        }
    }
}
