package cn.ucloud.ufile.demo.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.ucloud.ufile.demo.ui.activity.BaseActivity;
import cn.ucloud.ufile.demo.utils.JLog;

/**
 * Created by joshua on 2019/1/10 16:06.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public abstract class BaseFragment extends Fragment {
    protected final String TAG = getClass().getSimpleName();
    protected Context mContext;
    protected BaseActivity mActivity;
    public int size = 10;
    
    protected OnBaseFragmentListener mListener;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JLog.D(TAG, "onCreate--->[savedInstanceState]: "
                + (savedInstanceState == null ? "null" : savedInstanceState.toString()));
        
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (inflater == null)
            return super.onCreateView(inflater, container, savedInstanceState);
        
        JLog.D(TAG, "onCreateView--->[savedInstanceState]: "
                + (savedInstanceState == null ? "null" : savedInstanceState.toString()));
        return inflater.inflate(getContentViewId(), container, false);
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        JLog.D(TAG, "onViewCreated--->[savedInstanceState]: "
                + (savedInstanceState == null ? "null" : savedInstanceState.toString()));
        bindWidget(view);
        initData();
        initUI();
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (mContext instanceof BaseActivity)
            mActivity = (BaseActivity) mContext;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            mContext = activity;
        
        if (activity instanceof BaseActivity)
            mActivity = (BaseActivity) activity;
    }
    
    @Override
    public void onStart() {
        JLog.D(TAG, "onStart--->");
        super.onStart();
    }
    
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        JLog.T(TAG, "onViewStateRestored--->[savedInstanceState]:"
                + (savedInstanceState == null ? "null" : savedInstanceState.toString()));
        super.onViewStateRestored(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        JLog.T(TAG, "onSaveInstanceState--->[outState]: "
                + (outState == null ? "null" : outState.toString()));
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume() {
        JLog.D(TAG, "onResume--->");
        super.onResume();
        onMyResume();
    }
    
    protected void onMyResume() {
        JLog.D(TAG, "onMyResume--->");
    }
    
    @Override
    public void onPause() {
        JLog.D(TAG, "onPause--->");
        super.onPause();
    }
    
    @Override
    public void onStop() {
        JLog.D(TAG, "onStop--->");
        super.onStop();
    }
    
    @Override
    public void onDestroyView() {
        JLog.D(TAG, "onDestroyView--->");
        super.onDestroyView();
    }
    
    @Override
    public void onDestroy() {
        JLog.D(TAG, "onDestroy--->");
        super.onDestroy();
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        JLog.D(TAG, "onHiddenChanged--->[hidden]: " + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden)
            onMyResume();
    }
    
    protected abstract int getContentViewId();
    
    protected abstract void bindWidget(View contentView);
    
    protected abstract void initData();
    
    protected abstract void initUI();
    
    public interface OnBaseFragmentListener {
        void onFinish(Intent newIntent);
    }
    
    public void setListener(OnBaseFragmentListener listener) {
        this.mListener = listener;
    }
    
}
