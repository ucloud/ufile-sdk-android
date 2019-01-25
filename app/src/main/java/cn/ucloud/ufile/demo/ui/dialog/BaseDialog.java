package cn.ucloud.ufile.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Joshua_Yin on 2018/1/4 12:14.
 * Company: Gemii Tech
 * E-mail: jun.yin@gemii.cc
 */

public abstract class BaseDialog extends Dialog {
    protected final String TAG = getClass().getSimpleName();
    protected Context mContext;
    
    public BaseDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }
    
    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init();
    }
    
    protected BaseDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        init();
    }
    
    private void init() {
        setContentView(getContentViewId());
        bindWidget();
    }
    
    @Override
    public void show() {
        initData();
        initView();
        super.show();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    
    protected abstract int getContentViewId();
    
    protected abstract void bindWidget();
    
    protected abstract void initData();
    
    protected abstract void initView();
}
