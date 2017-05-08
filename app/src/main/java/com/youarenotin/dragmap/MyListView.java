package com.youarenotin.dragmap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.youarenotin.dragmap.ScrollViewListener.ObservableScrollViewCallbacks;
import com.youarenotin.dragmap.ScrollViewListener.ScrollState;

/**
 * Created by youarenotin on 2017/4/15.
 */

public class MyListView extends ListView implements ObservableScrollViewCallbacks {
    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (getFirstVisiblePosition()==0){
//            return false;
//        }
        return super.onTouchEvent(ev);
    }
}
