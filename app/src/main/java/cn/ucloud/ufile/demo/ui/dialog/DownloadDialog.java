package cn.ucloud.ufile.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.GenerateObjectPrivateUrlApi;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.DownloadStreamBean;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.utils.JLog;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.UfileCallback;
import okhttp3.Request;

/**
 * Created by joshua on 2019/1/24 15:27.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class DownloadDialog extends BaseDialog {
    private TextView txt_dialog_title;
    private TextView txt_dialog_info;
    private ProgressBar dialog_progress;
    private TextView txt_dialog_progress_info;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private String title;
    private String savePath;
    private ObjectProfile objectProfile;
    private ObjectApiBuilder objectApiBuilder;
    private OnDownloadDialogListener onDownloadDialogListener;
    
    public DownloadDialog(@NonNull Context context) {
        super(context);
    }
    
    public DownloadDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    protected DownloadDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.dialog_transmission_progress;
    }
    
    @Override
    protected void bindWidget() {
        title = getContext().getString(R.string.str_downloading);
        txt_dialog_title = findViewById(R.id.txt_dialog_title);
        txt_dialog_info = findViewById(R.id.txt_dialog_info);
        dialog_progress = findViewById(R.id.dialog_progress);
        dialog_progress.setMax(100);
        txt_dialog_progress_info = findViewById(R.id.txt_dialog_progress_info);
    }
    
    @Override
    protected void initData() {
    
    }
    
    @Override
    protected void initView() {
        txt_dialog_title.setText(title);
        txt_dialog_info.setText(objectProfile.getKeyName());
        dialog_progress.setProgress(0);
        txt_dialog_progress_info.setText("0 %");
    }
    
    @Override
    public void show() {
        super.show();
        doDownload();
    }
    
    private void doDownload() {
        if (objectProfile.getContentLength() > (8 << 20)) {
            objectApiBuilder.downloadFile(objectProfile).saveAt(savePath, objectProfile.getKeyName())
                    /**
                     * 设置是否覆盖本地文件，默认为覆盖
                     */
//                    .withCoverage(false)
                    /**
                     * 设置自定义进度回调监听，默认每秒回调
                     */
//                    .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                    .executeAsync(new UfileCallback<DownloadFileBean>() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            JLog.I(DownloadDialog.this.TAG, "onProgress--->" + (int) (bytesWritten * 1.f / contentLength * 100));
                            handler.post(() -> {
                                int progress = (int) (bytesWritten * 1.f / contentLength * 100);
                                dialog_progress.setProgress(progress);
                                txt_dialog_progress_info.setText(String.format("%d %%", progress));
                            });
                        }
                        
                        @Override
                        public void onResponse(DownloadFileBean response) {
                            handler.post(() -> {
                                if (onDownloadDialogListener != null)
                                    onDownloadDialogListener.onSuccess(DownloadDialog.this);
                            });
                        }
                        
                        @Override
                        public void onError(Request request, ApiError error, UfileErrorBean response) {
                            handler.post(() -> {
                                if (onDownloadDialogListener != null)
                                    onDownloadDialogListener.onFailed(DownloadDialog.this, response == null ? error.toString() : response.getErrMsg());
                            });
                        }
                    });
        } else {
            objectApiBuilder.getDownloadUrlFromPrivateBucket(objectProfile.getKeyName(), objectProfile.getBucket(), 60 * 60)
                    .createUrlAsync(new GenerateObjectPrivateUrlApi.CreatePrivateUrlCallback() {
                        @Override
                        public void onSuccess(String url) {
                            objectApiBuilder.getFile(url).saveAt(savePath, objectProfile.getKeyName())
                                    /**
                                     * 设置是否覆盖本地文件，默认为覆盖
                                     */
//                                    .withCoverage(false)
                                    /**
                                     * 设置自定义进度回调监听，默认每秒回调
                                     */
//                                    .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                                    .executeAsync(new UfileCallback<DownloadFileBean>() {
                                        @Override
                                        public void onProgress(long bytesWritten, long contentLength) {
                                            JLog.I(DownloadDialog.this.TAG, "onProgress--->" + (int) (bytesWritten * 1.f / contentLength * 100));
                                            handler.post(() -> {
                                                int progress = (int) (bytesWritten * 1.f / contentLength * 100);
                                                dialog_progress.setProgress(progress);
                                                txt_dialog_progress_info.setText(String.format("%d %%", progress));
                                            });
                                        }
                                        
                                        @Override
                                        public void onResponse(DownloadFileBean response) {
                                            handler.post(() -> {
                                                if (onDownloadDialogListener != null)
                                                    onDownloadDialogListener.onSuccess(DownloadDialog.this);
                                            });
                                        }
                                        
                                        @Override
                                        public void onError(Request request, ApiError error, UfileErrorBean response) {
                                            handler.post(() -> {
                                                if (onDownloadDialogListener != null)
                                                    onDownloadDialogListener.onFailed(DownloadDialog.this, response == null ? error.toString() : response.getErrMsg());
                                                
                                            });
                                        }
                                    });
                            
                            /**
                             * 也可以选择使用下载流，若需要将流保存到本地，可以使用重定向流的参数配置:redirectStream(OutputStream)
                             */
                            /*
                            try {
                                objectApiBuilder.getStream(url)
                                        .redirectStream(new FileOutputStream(new File(savePath, objectProfile.getKeyName())))
                                        .executeAsync(new UfileCallback<DownloadStreamBean>() {
                                            @Override
                                            public void onProgress(long bytesWritten, long contentLength) {
                                                JLog.I(DownloadDialog.this.TAG, "onProgress--->" + (int) (bytesWritten * 1.f / contentLength * 100));
                                                handler.post(() -> {
                                                    int progress = (int) (bytesWritten * 1.f / contentLength * 100);
                                                    dialog_progress.setProgress(progress);
                                                    txt_dialog_progress_info.setText(String.format("%d %%", progress));
                                                });
                                            }
                                            
                                            @Override
                                            public void onResponse(DownloadStreamBean response) {
                                                handler.post(() -> {
                                                    if (onDownloadDialogListener != null)
                                                        onDownloadDialogListener.onSuccess(DownloadDialog.this);
                                                });
                                            }
                                            
                                            @Override
                                            public void onError(Request request, ApiError error, UfileErrorBean response) {
                                                handler.post(() -> {
                                                    if (onDownloadDialogListener != null)
                                                        onDownloadDialogListener.onFailed(DownloadDialog.this, response == null ? error.toString() : response.getErrMsg());
                                                    
                                                });
                                            }
                                        });
                            } catch (FileNotFoundException e) {
                                if (onDownloadDialogListener != null)
                                    onDownloadDialogListener.onFailed(DownloadDialog.this, e.getMessage());
                            }
                            */
                        }
                        
                        @Override
                        public void onFailed(UfileClientException e) {
                            if (onDownloadDialogListener != null)
                                onDownloadDialogListener.onFailed(DownloadDialog.this, e.getMessage());
                        }
                    });
        }
    }
    
    public void setObjectProfile(ObjectProfile objectProfile) {
        this.objectProfile = objectProfile;
    }
    
    public void setOnDownloadDialogListener(OnDownloadDialogListener onDownloadDialogListener) {
        this.onDownloadDialogListener = onDownloadDialogListener;
    }
    
    @Override
    public void setTitle(int titleId) {
        this.setTitle(getContext().getString(titleId));
    }
    
    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title = title == null ? null : title.toString();
    }
    
    public interface OnDownloadDialogListener {
        void onSuccess(Dialog dialog);
        
        void onFailed(Dialog dialog, String errMsg);
    }
    
    public static class Builder {
        private Context context;
        private Integer themeResId;
        private String savePath;
        private ObjectProfile objectProfile;
        private ObjectApiBuilder objectApiBuilder;
        private boolean isCancelable, isOutsideTouchCancelable;
        private OnDownloadDialogListener onDownloadDialogListener;
        
        public Builder(Context context) {
            this.context = context;
        }
        
        public Builder(Context context, int themeResId) {
            this.context = context;
            this.themeResId = themeResId;
        }
        
        public Builder setObjectProfile(ObjectProfile objectProfile) {
            this.objectProfile = objectProfile;
            return this;
        }
        
        public Builder setSavePath(String savePath) {
            this.savePath = savePath;
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
        
        public Builder setOnDownloadDialogListener(OnDownloadDialogListener onDownloadDialogListener) {
            this.onDownloadDialogListener = onDownloadDialogListener;
            return this;
        }
        
        public Builder setObjectApiBuilder(ObjectApiBuilder objectApiBuilder) {
            this.objectApiBuilder = objectApiBuilder;
            return this;
        }
        
        public DownloadDialog create() {
            DownloadDialog dialog = themeResId == null ? new DownloadDialog(context) : new DownloadDialog(context, themeResId);
            dialog.objectProfile = objectProfile;
            dialog.savePath = savePath;
            dialog.objectApiBuilder = objectApiBuilder;
            dialog.onDownloadDialogListener = onDownloadDialogListener;
            dialog.setCanceledOnTouchOutside(isOutsideTouchCancelable);
            dialog.setCancelable(isCancelable);
            
            return dialog;
        }
    }
}
