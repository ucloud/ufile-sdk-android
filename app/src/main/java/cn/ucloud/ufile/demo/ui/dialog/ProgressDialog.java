package cn.ucloud.ufile.demo.ui.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.ucloud.ufile.demo.R;

/**
 * Created by joshua on 2019/1/23 15:01.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class ProgressDialog extends BaseDialog {
    private TextView txt_dialog_title, txt_dialog_message;
    private ProgressBar dialog_progress;
    
    private String title, message;
    
    private ProgressDialog(@NonNull Context context) {
        super(context);
    }
    
    private ProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    private ProgressDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.dialog_progress;
    }
    
    @Override
    protected void bindWidget() {
        txt_dialog_title = findViewById(R.id.txt_dialog_title);
        txt_dialog_message = findViewById(R.id.txt_dialog_message);
        dialog_progress = findViewById(R.id.dialog_progress);
    }
    
    @Override
    protected void initData() {
    
    }
    
    @Override
    protected void initView() {
        if (TextUtils.isEmpty(title)) {
            txt_dialog_title.setVisibility(View.GONE);
        } else {
            txt_dialog_title.setVisibility(View.VISIBLE);
            txt_dialog_title.setText(title);
        }
        
        if (TextUtils.isEmpty(message)) {
            txt_dialog_message.setVisibility(View.GONE);
        } else {
            txt_dialog_message.setVisibility(View.VISIBLE);
            txt_dialog_message.setText(message);
        }
    }
    
    @Override
    public void setTitle(@StringRes int titleId) {
        super.setTitle(titleId);
        this.setTitle(getContext().getString(titleId));
    }
    
    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title = title == null ? null : title.toString();
    }
    
    public void setMessage(@StringRes int messageId) {
        this.setMessage(getContext().getString(messageId));
    }
    
    public void setMessage(CharSequence message) {
        this.message = message == null ? null : message.toString();
    }
    
    public static class Builder {
        private Context context;
        private Integer themeResId;
        private String title, message;
        private boolean isCancelable, isOutsideTouchCancelable;
        
        public Builder(Context context) {
            this.context = context;
        }
        
        public Builder(Context context, int themeResId) {
            this.context = context;
            this.themeResId = themeResId;
        }
        
        public Builder setTitle(@StringRes int title) {
            return setTitle(context.getString(title));
        }
        
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Builder setMessage(@StringRes int message) {
            return setMessage(context.getString(message));
        }
        
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
        
        public Builder setCancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }
        
        public Builder setOutsideTouchCancelable(boolean isOutsideTouchCancelable) {
            this.isOutsideTouchCancelable = isOutsideTouchCancelable;
            return this;
        }
        
        public ProgressDialog create() {
            ProgressDialog dialog = themeResId == null ? new ProgressDialog(context) : new ProgressDialog(context, themeResId);
            dialog.title = title;
            dialog.message = message;
            dialog.setCanceledOnTouchOutside(isOutsideTouchCancelable);
            dialog.setCancelable(isCancelable);
            
            return dialog;
        }
    }
}
