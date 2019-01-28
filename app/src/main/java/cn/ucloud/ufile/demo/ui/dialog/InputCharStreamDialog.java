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
public class InputCharStreamDialog extends BaseDialog implements View.OnClickListener {
    private TextView txt_dialog_title;
    private AppCompatEditText edit_input_name;
    private AppCompatEditText edit_input_content;
    private TextView txt_dialog_cancel, txt_dialog_ok;
    
    private String title, content, hint;
    
    private OnDialogInputCharStreamListener onDialogInputCharStreamListener;
    
    private InputCharStreamDialog(@NonNull Context context) {
        super(context);
    }
    
    private InputCharStreamDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    private InputCharStreamDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.dialog_input_char_stream;
    }
    
    @Override
    protected void bindWidget() {
        txt_dialog_title = findViewById(R.id.txt_dialog_title);
        edit_input_name = findViewById(R.id.edit_input_name);
        edit_input_content = findViewById(R.id.edit_input_content);
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
    
    public void setOnDialogInputCharStreamListener(OnDialogInputCharStreamListener onDialogInputCharStreamListener) {
        this.onDialogInputCharStreamListener = onDialogInputCharStreamListener;
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
        
        edit_input_content.setHint(hint == null ? "" : hint);
        edit_input_content.setText(content == null ? "" : content);
        edit_input_content.setSelection(edit_input_content.getText().length());
    }
    
    @Override
    public void onClick(View v) {
        if (onDialogInputCharStreamListener == null)
            return;
        
        switch (v.getId()) {
            case R.id.txt_dialog_cancel: {
                onDialogInputCharStreamListener.onCancel(this);
                break;
            }
            case R.id.txt_dialog_ok: {
                onDialogInputCharStreamListener.onFinish(this, edit_input_name.getText().toString(), edit_input_content.getText().toString());
                break;
            }
        }
    }
    
    public interface OnDialogInputCharStreamListener {
        void onFinish(Dialog dialog, CharSequence title, CharSequence content);
        
        void onCancel(Dialog dialog);
    }
    
    public static class Builder {
        private Context context;
        private Integer themeResId;
        private String title, defaultContent, hint;
        private boolean isCancelable, isOutsideTouchCancelable;
        private OnDialogInputCharStreamListener onDialogInputListener;
        
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
        
        public Builder setOnDialogInputListener(OnDialogInputCharStreamListener onDialogInputListener) {
            this.onDialogInputListener = onDialogInputListener;
            return this;
        }
        
        public InputCharStreamDialog create() {
            InputCharStreamDialog dialog = themeResId == null ? new InputCharStreamDialog(context) : new InputCharStreamDialog(context, themeResId);
            dialog.title = title;
            dialog.content = defaultContent;
            dialog.hint = hint;
            dialog.setCanceledOnTouchOutside(isOutsideTouchCancelable);
            dialog.setCancelable(isCancelable);
            dialog.onDialogInputCharStreamListener = onDialogInputListener;
            
            return dialog;
        }
    }
}
