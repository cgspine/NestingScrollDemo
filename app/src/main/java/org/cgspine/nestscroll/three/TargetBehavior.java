package org.cgspine.nestscroll.three;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.Scroller;

/**
 * @author cginechen
 * @date 2016-12-29
 */

public class TargetBehavior extends CoordinatorLayout.Behavior<View> {
    private static final String TAG = "TargetBehavior";
    private int mTargetInitOffset;
    private int mTargetCurrentOffset;
    private int mTargetEndOffset = 0;
    private boolean mHasFling = false;
    private boolean mNeedScrollToInitPos = false;
    private boolean mNeedScrollToEndPos = false;
    private Scroller mScroller;

    public TargetBehavior(Context context, int initOffset, int endOffset) {
        mTargetInitOffset = initOffset;
        mTargetEndOffset = endOffset;
        mTargetCurrentOffset = mTargetInitOffset;
        mScroller = new Scroller(context);
        mScroller.setFriction(0.98f);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if (params != null && params.height == CoordinatorLayout.LayoutParams.MATCH_PARENT) {
            child.layout(0, mTargetCurrentOffset, parent.getWidth(), parent.getHeight() + mTargetCurrentOffset);
            return true;
        }

        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target,
                                  int dx, int dy, int[] consumed) {
        Log.i(TAG, "onNestedPreScroll: dy = " + dy);
        // 在这个方法里面只处理向上滑动
        if (canViewScrollUp(target) || dy <= 0) {
            return;
        }
        // 在这个方法里只处理上滑
        if (dy > 0) {
            int parentCanConsume = mTargetCurrentOffset - mTargetEndOffset;
            if (parentCanConsume > 0) {
                if (dy > parentCanConsume) {
                    consumed[1] = parentCanConsume;
                    moveTargetViewTo(child, mTargetEndOffset);
                } else {
                    consumed[1] = dy;
                    moveTargetView(child, -dy);
                }
            }
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i(TAG, "onNestedScroll: dyUnconsumed = " + dyUnconsumed);
        // 在这个方法里只处理向下滑动
        if (dyUnconsumed < 0 && !(canViewScrollUp(target))) {
            int dy = -dyUnconsumed;
            moveTargetView(child, dy);
        }
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target,
                                    float velocityX, float velocityY) {
        Log.i(TAG, "onNestedPreFling: mTargetCurrentOffset = " + mTargetCurrentOffset +
                " ; velocityX = " + velocityX + " ; velocityY = " + velocityY);
        mHasFling = true;
        int vy = (int) -velocityY;
        if (velocityY < 0) {
            // 向下
            if (canViewScrollUp(target)) {
                return false;
            }
            mNeedScrollToInitPos = true;
            mScroller.fling(0, mTargetCurrentOffset, 0, vy,
                    0, 0, mTargetEndOffset, Integer.MAX_VALUE);
            ViewCompat.postOnAnimation(child, new ScrollAction(child));
            return true;
        } else {
            // 向上
            if (mTargetCurrentOffset <= mTargetEndOffset) {
                return false;
            }
            mNeedScrollToEndPos = true;
            mScroller.fling(0, mTargetCurrentOffset, 0, vy,
                    0, 0, mTargetEndOffset, Integer.MAX_VALUE);
            ViewCompat.postOnAnimation(child, new ScrollAction(child));
        }
        return false;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        if (mHasFling) {
            mHasFling = false;
        } else {
            if (mTargetCurrentOffset <= (mTargetEndOffset + mTargetInitOffset) / 2) {

                mNeedScrollToEndPos = true;
            } else {
                mNeedScrollToInitPos = true;
            }
            ViewCompat.postOnAnimation(child, new ScrollAction(child));
        }
    }

    private boolean canViewScrollUp(View view) {
        return ViewCompat.canScrollVertically(view, -1);
    }

    private void moveTargetView(View child, int dy) {
        int target = mTargetCurrentOffset + dy;
        moveTargetViewTo(child, target);
    }

    private void moveTargetViewTo(View child, int target) {
        target = Math.max(target, mTargetEndOffset);
        ViewCompat.offsetTopAndBottom(child, target - mTargetCurrentOffset);
        mTargetCurrentOffset = target;
    }

    public int getTargetCurrentOffset() {
        return mTargetCurrentOffset;
    }

    public int getTargetInitOffset() {
        return mTargetInitOffset;
    }

    public int getTargetEndOffset() {
        return mTargetEndOffset;
    }

    private class ScrollAction implements Runnable {
        private View mView;

        public ScrollAction(View view) {
            mView = view;
        }

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                int offsetY = mScroller.getCurrY();
                moveTargetViewTo(mView, offsetY);
                ViewCompat.postOnAnimation(mView, new ScrollAction(mView));
            } else if (mNeedScrollToInitPos) {
                mNeedScrollToInitPos = false;
                if (mTargetCurrentOffset == mTargetInitOffset) {
                    return;
                }
                mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetInitOffset - mTargetCurrentOffset);
                ViewCompat.postOnAnimation(mView, new ScrollAction(mView));
            } else if (mNeedScrollToEndPos) {
                mNeedScrollToEndPos = false;
                if (mTargetCurrentOffset == mTargetEndOffset) {
                    return;
                }
                mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetEndOffset - mTargetCurrentOffset);
                ViewCompat.postOnAnimation(mView, new ScrollAction(mView));
            }
        }
    }
}
