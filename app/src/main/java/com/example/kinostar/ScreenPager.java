package com.example.kinostar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class ScreenPager extends ViewPager {
    private PagerAdapter adapter = new ScreenPagerAdapter();
    private List<View> viewList = new ArrayList<View>();
    private Context context;

    public ScreenPager(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ScreenPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private void init() {
        setAdapter(adapter);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addScreen(child);
    }

    public void addScreen(View screen) {
        viewList.add(screen);
        adapter.notifyDataSetChanged();
    }

    private class ScreenPagerAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    static LayoutInflater.Factory getShortNameFactory() {
        return new LayoutInflater.Factory() {
            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                if (ScreenPager.class.getSimpleName().equals(name)) {
                    return new ScreenPager(context, attrs);
                }
                return null;
            }
        };
    }

    public void changeState(){
        View view = LayoutInflater.from(context).inflate(R.layout.hello_member,null);
        ImageView first = view.findViewById(R.id.first);
        ImageView second = view.findViewById(R.id.second);
        ImageView third = view.findViewById(R.id.third);
        switch (getCurrentItem()){
            case 2:
                first.setImageResource(R.drawable.gray_circle);
                second.setImageResource(R.drawable.gray_circle);
                third.setImageResource(R.drawable.start_circle);
                Toast.makeText(context,"case 2",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                first.setImageResource(R.drawable.gray_circle);
                second.setImageResource(R.drawable.start_circle);
                third.setImageResource(R.drawable.gray_circle);
                Toast.makeText(context,"case 1",Toast.LENGTH_SHORT).show();
                break;
            case 0:
                first.setImageResource(R.drawable.start_circle);
                second.setImageResource(R.drawable.gray_circle);
                third.setImageResource(R.drawable.gray_circle);
                Toast.makeText(context,"case 0",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public int getCurrentItem() {
        return super.getCurrentItem();
    }
}