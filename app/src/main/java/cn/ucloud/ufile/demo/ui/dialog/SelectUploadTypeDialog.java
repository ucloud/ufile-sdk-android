package cn.ucloud.ufile.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cn.ucloud.ufile.demo.R;

/**
 * Created by joshua on 2019/1/23 15:01.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class SelectUploadTypeDialog extends BaseDialog implements View.OnClickListener {
    private TextView txt_dialog_title;
    private TextView txt_upload_file, txt_upload_stream;
    
    private String title;
    
    private OnSelectUploadTypeListener onSelectUploadTypeListener;
    
    public SelectUploadTypeDialog(@NonNull Context context) {
        super(context);
    }
    
    public SelectUploadTypeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    public SelectUploadTypeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.dialog_upload_type_select;
    }
    
    @Override
    protected void bindWidget() {
        txt_dialog_title = findViewById(R.id.txt_dialog_title);
        txt_upload_file = findViewById(R.id.txt_upload_file);
        txt_upload_stream = findViewById(R.id.txt_upload_stream);
        
        txt_upload_file.setOnClickListener(this);
        txt_upload_stream.setOnClickListener(this);
    }
    
    @Override
    public void setTitle(int titleId) {
        this.setTitle(getContext().getString(titleId));
    }
    
    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title = title == null ? null : title.toString();
    }
    
    public SelectUploadTypeDialog setOnSelectUploadTypeListener(OnSelectUploadTypeListener onSelectUploadTypeListener) {
        this.onSelectUploadTypeListener = onSelectUploadTypeListener;
        return this;
    }
    
    @Override
    protected void initData() {
        title = getContext().getString(R.string.str_select_upload_type);
    }
    
    @Override
    protected void initView() {
        if (TextUtils.isEmpty(title)) {
            txt_dialog_title.setVisibility(View.GONE);
        } else {
            txt_dialog_title.setVisibility(View.VISIBLE);
            txt_dialog_title.setText(title);
        }
        
    }
    
    @Override
    public void onClick(View v) {
        if (onSelectUploadTypeListener == null)
            return;
        
        switch (v.getId()) {
            case R.id.txt_upload_file: {
                onSelectUploadTypeListener.onSelect(this, OnSelectUploadTypeListener.UPLOAD_TYPE_FILE);
                break;
            }
            case R.id.txt_upload_stream: {
                onSelectUploadTypeListener.onSelect(this, OnSelectUploadTypeListener.UPLOAD_TYPE_STREAM);
                break;
            }
        }
    }
    
    public interface OnSelectUploadTypeListener {
        int UPLOAD_TYPE_FILE = 0;
        int UPLOAD_TYPE_STREAM = 1;
        
        void onSelect(Dialog dialog, int type);
    }
    
}
