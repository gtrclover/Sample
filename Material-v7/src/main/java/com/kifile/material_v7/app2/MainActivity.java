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

package com.kifile.material_v7.app2;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewAnimationUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements RecyclerView.OnItemTouchListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> mTitles = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
//        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnItemTouchListener(this);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerAdapter(this, mTitles);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initData() {
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");
        mTitles.add("CardView");
        mTitles.add("CardView2");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

    }
}
