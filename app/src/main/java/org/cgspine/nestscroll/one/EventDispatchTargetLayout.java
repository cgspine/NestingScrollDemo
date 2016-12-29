package org.cgspine.nestscroll.one;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.cgspine.nestscroll.R;

import java.util.ArrayList;

/**
 * @author cginechen
 * @date 2016-12-28
 */

public class EventDispatchTargetLayout extends LinearLayout
        implements EventDispatchPlanLayout.ITargetView {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SparseArray<ListView> mPageMap = new SparseArray<>();
    private ArrayList<String> mData = new ArrayList<>();


    private ListView getPageView(int pos) {
        ListView view = mPageMap.get(pos);
        if (view == null) {
            ListView listView = new ListView(getContext());
            listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mData));
            mPageMap.put(pos, listView);
            return listView;
        }
        return view;
    }

    public EventDispatchTargetLayout(Context context) {
        this(context, null);
    }

    public EventDispatchTargetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        for (int i = 0; i < 50; i++) {
            mData.add("item " + i);
        }
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            View view = getPageView(position);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "item " + (position + 1);
        }
    };

    @Override
    public boolean canChildScrollUp() {
        if (mViewPager == null) {
            return false;
        }
        int currentItem = mViewPager.getCurrentItem();
        ListView listView = mPageMap.get(currentItem);
        if (listView == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return listView.getChildCount() > 0
                    && (listView.getFirstVisiblePosition() > 0 || listView.getChildAt(0)
                    .getTop() < listView.getPaddingTop());
        } else {
            return ViewCompat.canScrollVertically(listView, -1);
        }
    }

    @Override
    public void fling(float vy) {
        if (mViewPager == null) {
            return;
        }
        int currentItem = mViewPager.getCurrentItem();
        ListView listView = mPageMap.get(currentItem);
        if (listView == null) {
            return;
        }
        if(android.os.Build.VERSION.SDK_INT >= 21){
            listView.fling((int) -vy);
        }else {
            // 可以调动第三方的实现，这里就先不管了
        }

    }
}
