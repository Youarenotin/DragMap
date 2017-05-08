
package com.youarenotin.dragmap;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ScrollView;


import com.youarenotin.dragmap.ScrollViewListener.ObservableScrollViewCallbacks;
import com.youarenotin.dragmap.ScrollViewListener.ScrollState;
import com.youarenotin.dragmap.ScrollViewListener.Scrollable;

import java.util.ArrayList;
import java.util.List;

/**
 * ScrollView that its scroll position can be observed.
 */
public class ObservableScrollView extends ScrollView implements Scrollable {

    // Fields that should be saved onSaveInstanceState
    private int mPrevScrollY;
    private int mScrollY;

    // Fields that don't need to be saved onSaveInstanceState
    private ObservableScrollViewCallbacks mCallbacks;
    private List<ObservableScrollViewCallbacks> mCallbackCollection;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;
    private boolean mIntercepted;
    private MotionEvent mPrevMoveEvent;
    private ViewGroup mTouchInterceptionViewGroup;
    private View scaleView;//可拉伸视图
    private View scaleViewBeta;
    private int defaultScaleHeight = 0;

    public void setScaleView(View view) {
        this.scaleView = view;
        //手动调用测量方法
        scaleView.measure(0, 0);
        //调用measure方法之后才能获取宽高,否则的话getMeasuredHeight()=0
        defaultScaleHeight = scaleView.getLayoutParams().height;
    }

    public void setScaleViewBeta(View view) {
        this.scaleViewBeta = view;
        //手动调用测量方法
    }

    public ObservableScrollView(Context context) {
        this(context, null);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    boolean mFlag;

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
//        //下拉过度时
//        if (deltaY < 0 && isTouchEvent && scrollY <= 0) {
//            //放大scaleView
//            scaleView.getLayoutParams().height = scaleView.getHeight() - deltaY / 2;
//            scaleView.requestLayout();
//            scaleViewBeta.getLayoutParams().height = scaleViewBeta.getHeight() - deltaY / 2;
//            scaleViewBeta.requestLayout();
//            mFlag = true;
//        }
//
//        //一种特殊情况：当放大后的scaleView.height+listview.height=屏幕高度时，再往上推时，会触发上拉过度，而不会调用onScrollChanged中的缩小方法
//        if (deltaY > 0 && scaleView.getHeight() > defaultScaleHeight) {
//            //缩小scaleView
//            scaleView.getLayoutParams().height = scaleView.getHeight() - deltaY / 2;
//            scaleView.requestLayout();
//            scaleViewBeta.getLayoutParams().height = scaleViewBeta.getHeight() - deltaY / 2;
//            scaleViewBeta.requestLayout();
//            mFlag = true;
//        }
//        if (scaleView.getHeight() <= defaultScaleHeight)
//            mFlag = false;
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        mPrevScrollY = ss.prevScrollY;
        mScrollY = ss.scrollY;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.prevScrollY = mPrevScrollY;
        ss.scrollY = mScrollY;
        return ss;
    }

    boolean flag = false;

    @Override
    protected void onScrollChanged(int l, final int t, int oldl, int oldt) {
        if (hasNoCallbacks()) {
            return;
        }
        mScrollY = t;

        dispatchOnScrollChanged(t, mFirstScroll, mDragging);
        if (mFirstScroll) {
            mFirstScroll = false;
        }

        if (mPrevScrollY < t) {
            mScrollState = ScrollState.UP;
        } else if (t < mPrevScrollY) {
            mScrollState = ScrollState.DOWN;
            //} else {
            // Keep previous state while dragging.
            // Never makes it STOP even if scrollY not changed.
            // Before Android 4.4, onTouchEvent calls onScrollChanged directly for ACTION_MOVE,
            // which makes mScrollState always STOP when onUpOrCancelMotionEvent is called.
            // STOP state is now meaningless for ScrollView.
        }
        mPrevScrollY = t;

        if (scaleView.getHeight() > defaultScaleHeight) {
            //缩小scleView
            scaleView.getLayoutParams().height = scaleView.getHeight() - (t - oldt);
            //同时，将header.top设置为0,(之前其实有一部分跑到屏幕上面了)
            scaleView.requestLayout();
//            setOnTouchListener(new OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (event.getAction()==MotionEvent.ACTION_MOVE&&mFlag==false) {
//                        event.setAction(MotionEvent.ACTION_CANCEL);
//                        onTouchEvent(event);
//                    }
//                      onTouchEvent(event);
//                    return false;
//                }
//            });
        } else {
//            setOnTouchListener(new OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    return false;
//                }
//            });
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (hasNoCallbacks()) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // Whether or not motion events are consumed by children,
                // flag initializations which are related to ACTION_DOWN events should be executed.
                // Because if the ACTION_DOWN is consumed by children and only ACTION_MOVEs are
                // passed to parent (this view), the flags will be invalid.
                // Also, applications might implement initialization codes to onDownMotionEvent,
                // so call it here.
                mFirstScroll = mDragging = true;
                dispatchOnDownMotionEvent();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (hasNoCallbacks()) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_UP:
//                if (scaleView.getLayoutParams().height>defaultScaleHeight) {
                ResetAnimation animation = new ResetAnimation();
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(300);
                scaleView.startAnimation(animation);
                scaleViewBeta.startAnimation(animation);
//                }
            case MotionEvent.ACTION_CANCEL:


                mIntercepted = false;
                mDragging = false;
                dispatchOnUpOrCancelMotionEvent(mScrollState);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPrevMoveEvent == null) {
                    mPrevMoveEvent = ev;
                }
                float diffY = ev.getY() - mPrevMoveEvent.getY();

                mPrevMoveEvent = MotionEvent.obtainNoHistory(ev);
                if (getCurrentScrollY() - diffY <= 0) {
                    // Can't scroll anymore.

                    if (mIntercepted) {
                        // Already dispatched ACTION_DOWN event to parents, so stop here.
                        return false;
                    }

                    // Apps can set the interception target other than the direct parent.
                    final ViewGroup parent;
                    if (mTouchInterceptionViewGroup == null) {
                        parent = (ViewGroup) getParent();
                    } else {
                        parent = mTouchInterceptionViewGroup;
                    }

                    // Get offset to parents. If the parent is not the direct parent,
                    // we should aggregate offsets from all of the parents.
                    float offsetX = 0;
                    float offsetY = 0;
                    for (View v = this; v != null && v != parent; v = (View) v.getParent()) {
                        offsetX += v.getLeft() - v.getScrollX();
                        offsetY += v.getTop() - v.getScrollY();
                    }
                    final MotionEvent event = MotionEvent.obtainNoHistory(ev);
                    event.offsetLocation(offsetX, offsetY);

                    if (parent.onInterceptTouchEvent(event)) {
                        mIntercepted = true;

                        // If the parent wants to intercept ACTION_MOVE events,
                        // we pass ACTION_DOWN event to the parent
                        // as if these touch events just have began now.
                        event.setAction(MotionEvent.ACTION_DOWN);

                        // Return this onTouchEvent() first and set ACTION_DOWN event for parent
                        // to the queue, to keep events sequence.
                        post(new Runnable() {
                            @Override
                            public void run() {
                                parent.dispatchTouchEvent(event);
                            }
                        });
                        return false;
                    }
                    // Even when this can't be scrolled anymore,
                    // simply returning false here may cause subView's click,
                    // so delegate it to super.
                    return super.onTouchEvent(ev);
                }
                break;
        }
//        if (mFlag&&ev.getAction()==MotionEvent.ACTION_MOVE)
//             ev.setAction(MotionEvent.ACTION_CANCEL);

        return super.onTouchEvent(ev);
    }

    class ResetAnimation extends Animation {

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            //interpolatedTime:[0,1],表示执行百分比
            //缩小scleView
            scaleView.getLayoutParams().height = calcBetweenValue(scaleView.getLayoutParams().height, defaultScaleHeight, interpolatedTime);
            scaleView.requestLayout();
            scaleViewBeta.getLayoutParams().height = calcBetweenValue(scaleViewBeta.getLayoutParams().height, defaultScaleHeight, interpolatedTime);
            scaleViewBeta.requestLayout();
        }
    }

    private int calcBetweenValue(int src, int dest, float progress) {
        return (int) (src - (src - dest) * progress);
    }

    @Override
    public void setScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        mCallbacks = listener;
    }

    @Override
    public void addScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        if (mCallbackCollection == null) {
            mCallbackCollection = new ArrayList<>();
        }
        mCallbackCollection.add(listener);
    }

    @Override
    public void removeScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        if (mCallbackCollection != null) {
            mCallbackCollection.remove(listener);
        }
    }

    @Override
    public void clearScrollViewCallbacks() {
        if (mCallbackCollection != null) {
            mCallbackCollection.clear();
        }
    }

    @Override
    public void setTouchInterceptionViewGroup(ViewGroup viewGroup) {
        mTouchInterceptionViewGroup = viewGroup;
    }

    @Override
    public void scrollVerticallyTo(int y) {
        scrollTo(0, y);
    }

    @Override
    public int getCurrentScrollY() {
        return mScrollY;
    }

    private void dispatchOnDownMotionEvent() {
        if (mCallbacks != null) {
            mCallbacks.onDownMotionEvent();
        }
        if (mCallbackCollection != null) {
            for (int i = 0; i < mCallbackCollection.size(); i++) {
                ObservableScrollViewCallbacks callbacks = mCallbackCollection.get(i);
                callbacks.onDownMotionEvent();
            }
        }
    }

    private void dispatchOnScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (mCallbacks != null) {
            mCallbacks.onScrollChanged(scrollY, firstScroll, dragging);
        }
        if (mCallbackCollection != null) {
            for (int i = 0; i < mCallbackCollection.size(); i++) {
                ObservableScrollViewCallbacks callbacks = mCallbackCollection.get(i);
                callbacks.onScrollChanged(scrollY, firstScroll, dragging);
            }
        }
    }

    private void dispatchOnUpOrCancelMotionEvent(ScrollState scrollState) {
        if (mCallbacks != null) {
            mCallbacks.onUpOrCancelMotionEvent(scrollState);
        }
        if (mCallbackCollection != null) {
            for (int i = 0; i < mCallbackCollection.size(); i++) {
                ObservableScrollViewCallbacks callbacks = mCallbackCollection.get(i);
                callbacks.onUpOrCancelMotionEvent(scrollState);
            }
        }
    }

    private boolean hasNoCallbacks() {
        return mCallbacks == null && mCallbackCollection == null;
    }

    static class SavedState extends BaseSavedState {
        int prevScrollY;
        int scrollY;

        /**
         * Called by onSaveInstanceState.
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Called by CREATOR.
         */
        private SavedState(Parcel in) {
            super(in);
            prevScrollY = in.readInt();
            scrollY = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(prevScrollY);
            out.writeInt(scrollY);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
