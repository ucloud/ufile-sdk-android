package cn.ucloud.ufile.demo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

/**
 * Created by joshua on 2019/1/10 15:04.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();
    
    protected volatile WeakReference<Handler> mWeakHandler;
    protected InputMethodManager inputMethodManager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        
        bindWidget();
        initData();
        initUI();
    }
    
    protected abstract int getContentViewId();
    
    protected abstract void bindWidget();
    
    protected abstract void initData();
    
    protected abstract void initUI();
    
    protected synchronized Handler getHandler() {
        if (mWeakHandler == null || mWeakHandler.get() == null) {
            mWeakHandler = new WeakReference<>(new Handler(Looper.getMainLooper()));
        }
        
        return mWeakHandler.get();
    }
    
    protected void hideSoftInput() {
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    protected void toggleSoftInput() {
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
