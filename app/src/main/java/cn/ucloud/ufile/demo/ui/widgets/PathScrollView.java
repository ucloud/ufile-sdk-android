package cn.ucloud.ufile.demo.ui.widgets;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import cn.ucloud.ufile.demo.utils.JLog;
import cn.ucloud.ufile.demo.utils.ViewUtil;

/**
 * Created by joshua on 2019/1/24 10:50.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class PathScrollView extends HorizontalScrollView {
    private PathLinearLayout linearLayout;
    
    public PathScrollView(Context context) {
        super(context);
        init();
    }
    
    public PathScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PathScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PathScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    
    private void init() {
        linearLayout = new PathLinearLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(ViewUtil.dp2px(getContext(), 8), ViewUtil.dp2px(getContext(), 8), ViewUtil.dp2px(getContext(), 8), ViewUtil.dp2px(getContext(), 8));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout, params);
    }
    
    @Override
    public void addView(View child) {
        linearLayout.addView(child);
    }
    
    @Override
    public void addView(View child, int index) {
        linearLayout.addView(child, index);
    }
    
    @Override
    public void removeView(View view) {
        linearLayout.removeView(view);
    }
    
    public class PathLinearLayout extends LinearLayout {
        public PathLinearLayout(Context context) {
            super(context);
        }
        
        public PathLinearLayout(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }
        
        public PathLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
        
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public PathLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    
        @Override
        public void onViewAdded(View child) {
            super.onViewAdded(child);
            fullScroll(FOCUS_RIGHT);
        }
    
        @Override
        public void onViewRemoved(View child) {
            super.onViewRemoved(child);
            fullScroll(FOCUS_BACKWARD);
        }
    }
}
