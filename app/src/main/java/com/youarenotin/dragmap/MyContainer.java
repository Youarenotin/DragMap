package com.youarenotin.dragmap;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.style.LeadingMarginSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by youarenotin on 2017/4/15.
 */

public class MyContainer extends LinearLayout {
    private boolean isOnTop;
    private boolean isOnBottom;
    private float downY;
    private float moveY;
    private float disY;
    private Scroller scroller;
    private int mTopMargin;
    private int mScreenHight;
    private float upY;
    private Context mContext;
    private float mDensity;
    private TextView mTopTextView;
    private boolean isMove = false;
    private float ir_down_Y;
    private float ir_move_Y;
    private int mLastMotionY;
    private int mLastMotionX;
    private int globalDownY;
    private int mTouchSlop;
    private float downX;
    private float moveX;
    private boolean isAnim;


    public MyContainer(Context context) {
        super(context);
        init(context);
    }

    private ListView listener;

    public MyContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        Interpolator interpolator = new BounceInterpolator();
        scroller = new Scroller(context, interpolator);
        mScreenHight = getResources().getDisplayMetrics().heightPixels;
        mDensity = getResources().getDisplayMetrics().density;
        mTouchSlop = ViewConfiguration.getTouchSlop();
        post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
                mTopMargin = params.topMargin;
            }
        });


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int y = (int) e.getRawY();
        int x = (int) e.getRawX();
        boolean resume = false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 发生down事件时,记录y坐标
                mLastMotionY = y;
                mLastMotionX = x;
                globalDownY = y;
                resume = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // deltaY > 0 是向下运动,< 0是向上运动
                int deltaY = y - mLastMotionY;
                int deleaX = x - mLastMotionX;
                mLastMotionY=y;
                mLastMotionX=x;
//                if (Math.abs(deleaX) > Math.abs(deltaY)) {
//                    resume = true;
//                } else {
                    if (isAnim)
                        return true;
                    //当前正处于滑动
                    if (isOnTop) {
                        if (isRefreshViewScroll(deltaY)) {
                            return true;
                        }
                        return false;
                    }
                    if (isOnBottom)
                    {
                        return false;
                    }

//                }

                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                mLastMotionY=0;
//                mLastMotionX=0;
                break;
        }
        return resume;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getRawY();
                downX=  event.getRawX();
                isMove = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                moveY = event.getRawY();
                moveX = event.getRawX();
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
//                if (moveY-mLastMotionY<mTouchSlop)
//                    return false
                if (Math.abs(moveX-mLastMotionX)>Math.abs(moveY-mLastMotionY))
                    break;
                else if (moveY - mLastMotionY > 0) {
                    disY = moveY - mLastMotionY;
                    params.topMargin += disY;
                    params.bottomMargin = 0;
                } else if (moveY - mLastMotionY < 0 && isOnTop==false) {
                    disY = mLastMotionY - moveY;
                    params.topMargin -= disY;
                    params.bottomMargin = 0;
                }
                setLayoutParams(params);
//                requestLayout();
                mLastMotionY = (int) moveY;
                mLastMotionX= (int) moveX;
                break;
            case MotionEvent.ACTION_UP:
                upY = event.getRawY();
                final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
//                if (upY - globalDownY > (mScreenHight - mTopMargin) / 2) {
                if (layoutParams.topMargin > ((mScreenHight - 65 * mDensity - mTopMargin) / 2 + mTopMargin)) {//滑动到底部
                    DecelerateInterpolator interpolator = new DecelerateInterpolator();
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            layoutParams.topMargin = calcBetweenValue(layoutParams.topMargin, (int) (mScreenHight - mDensity * 65), interpolatedTime);
                            layoutParams.bottomMargin = calcBetweenValue(-(int) (mScreenHight - mTopMargin - 65 * mDensity), layoutParams.bottomMargin, interpolatedTime);
                            setLayoutParams(layoutParams);
                            requestLayout();
                        }
                    };
                    animation.setInterpolator(interpolator);
                    animation.setDuration(300);
                    startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            isOnTop = false;
                            isOnBottom=true;
                            isAnim=true;
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            isOnTop = false;
                            isOnBottom=true;
                            isAnim=false;
                            mTopTextView.setVisibility(VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else if (layoutParams.topMargin < mTopMargin * 2 / 3) {//滑动到顶部
                    DecelerateInterpolator interpolator = new DecelerateInterpolator();
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            layoutParams.topMargin = calcBetweenValue(layoutParams.topMargin, 0, interpolatedTime);
                            layoutParams.bottomMargin = calcBetweenValue(0, layoutParams.bottomMargin, interpolatedTime);
                            setLayoutParams(layoutParams);
                            requestLayout();
                        }
                    };
                    animation.setInterpolator(interpolator);
                    animation.setDuration(300);
                    startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            isOnTop = true;
                            isOnBottom=false;
                            isAnim=true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            isOnTop = true;
                            isOnBottom=false;
                            isAnim=false;
                            mTopTextView.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } //滑动到中间
                else if (layoutParams.topMargin < ((mScreenHight - 65 * mDensity - mTopMargin) / 2 + mTopMargin)&&layoutParams.topMargin>mTopMargin*2/3) {
                    DecelerateInterpolator interpolator = new DecelerateInterpolator();
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            layoutParams.topMargin = calcBetweenValue(layoutParams.topMargin, mTopMargin, interpolatedTime);
                            layoutParams.bottomMargin = calcBetweenValue(0, layoutParams.bottomMargin, interpolatedTime);
                            setLayoutParams(layoutParams);
                            requestLayout();
                        }
                    };
                    animation.setInterpolator(interpolator);
                    animation.setDuration(300);
                    startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            isOnTop = false;
                            isOnBottom=false;
                            isAnim=true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            isOnTop = false;
                            isOnBottom=false;
                            isAnim=false;
                            mTopTextView.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                downX=0;
                moveX=0;
                downY = 0;
                moveY = 0;
                disY = 0;
                isMove = false;
        }
        return super.onTouchEvent(event);
    }

    private int calcBetweenValue(int src, int dest, float progress) {
        return (int) (src - (src - dest) * progress);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {//如果滑动没有完成
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
//            isMove = true;
        } else {
//            isMove = false;
        }
        super.computeScroll();
    }


    public void addListView(ListView listener) {
        this.listener = listener;
        listener.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    Log.d("ListView", "##### 滚动到顶部 #####");
//                    isOnTop = true;
                } else if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    Log.d("ListView", "##### 滚动到底部 ######");
//                    isOnTop = false;
                }
            }
        });
    }

    public void setTopView(TextView tv) {
        this.mTopTextView = tv;
        mTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMove) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                    float fromY = layoutParams.topMargin;
                    float toY = mScreenHight - mDensity * 65 - mTopMargin;
                    if (layoutParams.topMargin == mTopMargin) {
//                        DecelerateInterpolator interpolator = new DecelerateInterpolator();
//                        Animation animation = new Animation() {
//                            @Override
//                            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
//                                layoutParams.topMargin = calcBetweenValue(layoutParams.topMargin, (int) (mScreenHight - 65 * mDensity), interpolatedTime);
//                                setLayoutParams(layoutParams);
//                                requestLayout();
//                            }
//                        };
//                        animation.setInterpolator(interpolator);
//                        animation.setDuration(300);
//                        animation.setAnimationListener(new Animation.AnimationListener() {
//                            @Override
//                            public void onAnimationStart(Animation animation) {
//                                mTopTextView.setVisibility(GONE);
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animation animation) {
//                                isOnBottom=true;
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animation animation) {
//
//                            }
//                        });
//                        startAnimation(animation);
                    } else {
                        DecelerateInterpolator interpolator = new DecelerateInterpolator();
                        Animation animation = new Animation() {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                                layoutParams.topMargin = calcBetweenValue(layoutParams.topMargin, mTopMargin, interpolatedTime);
                                setLayoutParams(layoutParams);
                                requestLayout();
                            }
                        };
                        animation.setInterpolator(interpolator);
                        animation.setDuration(300);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                mTopTextView.setVisibility(GONE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                isOnBottom=false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        startAnimation(animation);
                    }
                }
            }
        });
    }

    private boolean isRefreshViewScroll(int deltaY) {
        // 对于ListView和GridView
        if (listener != null) {
            // 子view(ListView or GridView)滑动到最顶端
            if (deltaY > 0) {
                View child = listener.getChildAt(0);
                if (child == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    return false;
                }
                if (listener.getFirstVisiblePosition() == 0
                        && child.getTop() == 0) {
                    return true;
                }
                int top = child.getTop();
                int padding = listener.getPaddingTop();
                if (listener.getFirstVisiblePosition() == 0
                        && Math.abs(top - padding) <= 8) {// 这里之前用3可以判断,但现在不行,还没找到原因
                    return true;
                }
            } else if (deltaY < 0) {
//                View lastChild = listener.getChildAt(listener
//                        .getChildCount() - 1);
//                if (lastChild == null) {
//                    // 如果mAdapterView中没有数据,不拦截
//                    return false;
//                }
//                // 最后一个子view的Bottom小于父View的高度说明mAdapterView的数据没有填满父view,
//                // 等于父View的高度说明mAdapterView已经滑动到最后
//                if (lastChild.getBottom() <= getHeight()
//                        && listener.getLastVisiblePosition() == listener
//                        .getCount() - 1) {
//                    return true;
//                }
            }
        }
        // 对于ScrollView
//        if (mScrollView != null) {
//            // 子scroll view滑动到最顶端
//            View child = mScrollView.getChildAt(0);
//            if (deltaY > 0 && mScrollView.getScrollY() == 0) {
//                mPullState = PULL_DOWN_STATE;
//                return true;
//            } else if (deltaY < 0
//                    && child.getMeasuredHeight() <= getHeight()
//                    + mScrollView.getScrollY()) {
//                mPullState = PULL_UP_STATE;
//                return true;
//            }
//        }
        return false;
    }
}
