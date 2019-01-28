package cn.ucloud.ufile.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cn.ucloud.ufile.demo.R;

/**
 * Created by joshua on 2019/1/23 15:01.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class InputDialog extends BaseDialog implements View.OnClickListener {
    private TextView txt_dialog_title;
    private AppCompatEditText edit_input;
    private TextView txt_dialog_cancel, txt_dialog_ok;
    
    private String title, content, hint;
    
    private OnDialogInputListener onDialogInputListener;
    
    private InputDialog(@NonNull Context context) {
        super(context);
    }
    
    private InputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    private InputDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.dialog_input;
    }
    
    @Override
    protected void bindWidget() {
        txt_dialog_title = findViewById(R.id.txt_dialog_title);
        edit_input = findViewById(R.id.edit_input);
        txt_dialog_cancel = findViewById(R.id.txt_dialog_cancel);
        txt_dialog_ok = findViewById(R.id.txt_dialog_ok);
        
        txt_dialog_cancel.setOnClickListener(this);
        txt_dialog_ok.setOnClickListener(this);
    }
    
    @Override
    public void setTitle(int titleId) {
        this.setTitle(getContext().getString(titleId));
    }
    
    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title = title == null ? null : title.toString();
    }
    
    public void setOnDialogInputListener(OnDialogInputListener onDialogInputListener) {
        this.onDialogInputListener = onDialogInputListener;
    }
    
    public void setContent(String content) {
        this.content = content;
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
        
        edit_input.setHint(hint == null ? "" : hint);
        edit_input.setText(content == null ? "" : content);
        edit_input.setSelection(edit_input.getText().length());
    }
    
    @Override
    public void onClick(View v) {
        if (onDialogInputListener == null)
            return;
        
        switch (v.getId()) {
            case R.id.txt_dialog_cancel: {
                onDialogInputListener.onCancel(this);
                break;
            }
            case R.id.txt_dialog_ok: {
                onDialogInputListener.onFinish(this, edit_input.getText().toString());
                break;
            }
        }
    }
    
    public interface OnDialogInputListener {
        void onFinish(Dialog dialog, CharSequence content);
        
        void onCancel(Dialog dialog);
    }
    
    public static class Builder {
        private Context context;
        private Integer themeResId;
        private String title, defaultContent, hint;
        private boolean isCancelable, isOutsideTouchCancelable;
        private OnDialogInputListener onDialogInputListener;
        
        public Builder(Context context) {
            this.context = context;
        }
        
        public Builder(Context context, int themeResId) {
            this.context = context;
            this.themeResId = themeResId;
        }
        
        public Builder setTitle(@StringRes int titleId) {
            return setTitle(context.getString(titleId));
        }
        
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Builder setDefaultContent(@StringRes int defaultContentId) {
            return setDefaultContent(context.getString(defaultContentId));
        }
        
        public Builder setDefaultContent(String message) {
            this.defaultContent = message;
            return this;
        }
        
        public Builder setHint(@StringRes int hintId) {
            setHint(context.getString(hintId));
            return this;
        }
        
        public Builder setHint(String hint) {
            this.hint = hint;
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
        
        public Builder setOnDialogInputListener(OnDialogInputListener onDialogInputListener) {
            this.onDialogInputListener = onDialogInputListener;
            return this;
        }
        
        public InputDialog create() {
            InputDialog dialog = themeResId == null ? new InputDialog(context) : new InputDialog(context, themeResId);
            dialog.title = title;
            dialog.content = defaultContent;
            dialog.hint = hint;
            dialog.setCanceledOnTouchOutside(isOutsideTouchCancelable);
            dialog.setCancelable(isCancelable);
            dialog.onDialogInputListener = onDialogInputListener;
            
            return dialog;
        }
    }
}
