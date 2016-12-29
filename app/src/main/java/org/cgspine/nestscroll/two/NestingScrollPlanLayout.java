package org.cgspine.nestscroll.two;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import org.cgspine.nestscroll.R;
import org.cgspine.nestscroll.Util;

/**
 * @author cginechen
 * @date 2016-12-28
 */

public class NestingScrollPlanLayout extends ViewGroup implements NestedScrollingParent {
    private static final String TAG = "NestingScrollPlanLayout";

    private int mHeaderViewId = 0;
    private int mTargetViewId = 0;
    private View mHeaderView;
    private View mTargetView;

    private int mHeaderInitOffset;
    private int mHeaderCurrentOffset;
    private int mHeaderEndOffset = 0;

    private int mTargetInitOffset;
    private int mTargetCurrentOffset;
    private int mTargetEndOffset = 0;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private Scroller mScroller;
    private boolean mNeedScrollToInitPos = false;
    private boolean mNeedScrollToEndPos = false;
    private boolean mHasFling = false;

    public NestingScrollPlanLayout(Context context) {
        this(context, null);
    }

    public NestingScrollPlanLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NestingScrollPlanLayout, 0, 0);
        mHeaderViewId = array.getResourceId(R.styleable.NestingScrollPlanLayout_header_view, 0);
        mTargetViewId = array.getResourceId(R.styleable.NestingScrollPlanLayout_target_view, 0);

        mHeaderInitOffset = array.getDimensionPixelSize(R.styleable.
                EventDispatchPlanLayout_header_init_offset, Util.dp2px(getContext(), 20));
        mTargetInitOffset = array.getDimensionPixelSize(R.styleable.
                EventDispatchPlanLayout_target_init_offset, Util.dp2px(getContext(), 40));
        mHeaderCurrentOffset = mHeaderInitOffset;
        mTargetCurrentOffset = mTargetInitOffset;
        array.recycle();

        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mScroller = new Scroller(getContext());
        mScroller.setFriction(0.98f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mHeaderViewId != 0) {
            mHeaderView = findViewById(mHeaderViewId);
        }
        if (mTargetViewId != 0) {
            mTargetView = findViewById(mTargetViewId);
        }
    }

    private void ensureHeaderViewAndScrollView() {
        if (mHeaderView != null && mTargetView != null) {
            return;
        }
        if (mHeaderView == null && mTargetView == null && getChildCount() >= 2) {
            mHeaderView = getChildAt(0);
            mTargetView = getChildAt(1);
            return;
        }
        throw new RuntimeException("please ensure headerView and scrollView");
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        ensureHeaderViewAndScrollView();
        int headerIndex = indexOfChild(mHeaderView);
        int scrollIndex = indexOfChild(mTargetView);
        if (headerIndex < scrollIndex) {
            return i;
        }
        if (headerIndex == i) {
            return scrollIndex;
        } else if (scrollIndex == i) {
            return headerIndex;
        }
        return i;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureHeaderViewAndScrollView();
        int scrollMeasureWidthSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        int scrollMeasureHeightSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTargetView.measure(scrollMeasureWidthSpec, scrollMeasureHeightSpec);
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        ensureHeaderViewAndScrollView();

        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        mTargetView.layout(childLeft, childTop + mTargetCurrentOffset,
                childLeft + childWidth, childTop + childHeight + mTargetCurrentOffset);
        int headerViewWidth = mHeaderView.getMeasuredWidth();
        int headerViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.layout((width / 2 - headerViewWidth / 2), mHeaderCurrentOffset,
                (width / 2 + headerViewWidth / 2), mHeaderCurrentOffset + headerViewHeight);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.i(TAG, "onStartNestedScroll: nestedScrollAxes = " + nestedScrollAxes);
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        Log.i(TAG, "onNestedScrollAccepted: axes = " + axes);
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.i(TAG, "onNestedPreScroll: dx = " + dx + " ; dy = " + dy);
        if (canViewScrollUp(target)) {
            return;
        }
        if (dy > 0) {
            // 往上滑
            int parentCanConsume = mTargetCurrentOffset - mTargetEndOffset;
            if (parentCanConsume > 0) {
                if (dy > parentCanConsume) {
                    consumed[1] = parentCanConsume;
                    moveTargetViewTo(mTargetEndOffset);
                } else {
                    consumed[1] = dy;
                    moveTargetView(-dy);
                }
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i(TAG, "onNestedScroll: dxConsumed = " + dxConsumed + " ; dyConsumed = " + dyConsumed +
                " ; dxUnconsumed = " + dxUnconsumed + " ; dyUnconsumed = " + dyUnconsumed);
        if (dyUnconsumed < 0 && !(canViewScrollUp(target))) {
            int dy = -dyUnconsumed;
            moveTargetView(dy);
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View child) {
        Log.i(TAG, "onStopNestedScroll");
        mNestedScrollingParentHelper.onStopNestedScroll(child);
        if (mHasFling) {
            mHasFling = false;
        } else {
            if (mTargetCurrentOffset <= (mTargetEndOffset + mTargetInitOffset) / 2) {
                mNeedScrollToEndPos = true;
            } else {
                mNeedScrollToInitPos = true;
            }
            invalidate();
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        super.onNestedPreFling(target, velocityX, velocityY);
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
            invalidate();
            return true;
        } else {
            // 向上
            if (mTargetCurrentOffset <= mTargetEndOffset) {
                return false;
            }
            mNeedScrollToEndPos = true;
            mScroller.fling(0, mTargetCurrentOffset, 0, vy,
                    0, 0, mTargetEndOffset, Integer.MAX_VALUE);
            invalidate();
        }
        return false;
    }


    private boolean canViewScrollUp(View view) {
        return ViewCompat.canScrollVertically(view, -1);
    }


    private void moveTargetView(float dy) {
        int target = mTargetCurrentOffset + (int) (dy);
        moveTargetViewTo(target);
    }

    private void moveTargetViewTo(int target) {
        target = Math.max(target, mTargetEndOffset);
        ViewCompat.offsetTopAndBottom(mTargetView, target - mTargetCurrentOffset);
        mTargetCurrentOffset = target;

        int headerTarget;
        if (mTargetCurrentOffset >= mTargetInitOffset) {
            headerTarget = mHeaderInitOffset;
        } else if (mTargetCurrentOffset <= mTargetEndOffset) {
            headerTarget = mHeaderEndOffset;
        } else {
            float percent = (mTargetCurrentOffset - mTargetEndOffset) * 1.0f / mTargetInitOffset - mTargetEndOffset;
            headerTarget = (int) (mHeaderEndOffset + percent * (mHeaderInitOffset - mHeaderEndOffset));
        }
        ViewCompat.offsetTopAndBottom(mHeaderView, headerTarget - mHeaderCurrentOffset);
        mHeaderCurrentOffset = headerTarget;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int offsetY = mScroller.getCurrY();
            moveTargetViewTo(offsetY);
            invalidate();
        } else if (mNeedScrollToInitPos) {
            mNeedScrollToInitPos = false;
            if (mTargetCurrentOffset == mTargetInitOffset) {
                return;
            }
            mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetInitOffset - mTargetCurrentOffset);
            invalidate();
        } else if (mNeedScrollToEndPos) {
            mNeedScrollToEndPos = false;
            if (mTargetCurrentOffset == mTargetEndOffset) {
                return;
            }
            mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetEndOffset - mTargetCurrentOffset);
            invalidate();
        }
    }
}
