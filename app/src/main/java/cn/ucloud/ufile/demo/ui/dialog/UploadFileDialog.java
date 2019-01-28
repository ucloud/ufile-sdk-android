package cn.ucloud.ufile.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.api.object.multi.FinishMultiUploadApi;
import cn.ucloud.ufile.api.object.multi.MultiUploadInfo;
import cn.ucloud.ufile.api.object.multi.MultiUploadPartState;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.utils.FileUtil;
import cn.ucloud.ufile.demo.utils.JLog;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.util.MimeTypeUtil;
import okhttp3.Request;

/**
 * Created by joshua on 2019/1/24 15:27.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class UploadFileDialog extends BaseDialog {
    private TextView txt_dialog_title;
    private TextView txt_dialog_info;
    private ProgressBar dialog_progress;
    private TextView txt_dialog_progress_info;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private String title;
    private File file;
    private long contentLength;
    private String targetBucketName;
    private ObjectApiBuilder objectApiBuilder;
    private OnUploadDialogListener onUploadDialogListener;
    
    public UploadFileDialog(@NonNull Context context) {
        super(context);
    }
    
    public UploadFileDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    protected UploadFileDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.dialog_transmission_progress;
    }
    
    @Override
    protected void bindWidget() {
        title = getContext().getString(R.string.str_uploading);
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
        txt_dialog_info.setText(file.getName());
        dialog_progress.setProgress(0);
        txt_dialog_progress_info.setText("0 %");
    }
    
    @Override
    public void show() {
        super.show();
        doUpload();
    }
    
    private void doUpload() {
        if (contentLength > (8 << 20)) {
            doMultiUpload();
        } else {
            objectApiBuilder.putObject(file, MimeTypeUtil.getMimeType(file.getName()))
                    .nameAs(file.getName())
                    .toBucket(targetBucketName)
                    /**
                     * 设置是否上传文件MD5校验码，默认为true
                     */
//                    .withVerifyMd5(false)
                    /**
                     * 设置自定义进度回调监听，默认每秒回调
                     */
//                    .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                    .executeAsync(new UfileCallback<PutObjectResultBean>() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            JLog.I(UploadFileDialog.this.TAG, "onProgress--->" + (int) (bytesWritten * 1.f / contentLength * 100));
                            handler.post(() -> {
                                int progress = (int) (bytesWritten * 1.f / contentLength * 100);
                                dialog_progress.setProgress(progress);
                                txt_dialog_progress_info.setText(String.format("%d %%", progress));
                            });
                        }
                        
                        @Override
                        public void onResponse(PutObjectResultBean response) {
                            handler.post(() -> {
                                if (onUploadDialogListener != null)
                                    onUploadDialogListener.onSuccess(UploadFileDialog.this);
                            });
                        }
                        
                        @Override
                        public void onError(Request request, ApiError error, UfileErrorBean response) {
                            handler.post(() -> {
                                if (onUploadDialogListener != null)
                                    onUploadDialogListener.onFailed(UploadFileDialog.this, response == null ? error.toString() : response.toString());
                                
                            });
                        }
                    });
        }
    }
    
    private List<AtomicLong> sentLengths;
    
    private void doMultiUpload() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        txt_dialog_progress_info.setText(R.string.str_doing_multipart_initialization);
        objectApiBuilder.initMultiUpload(file.getName(), MimeTypeUtil.getMimeType(file.getName()), targetBucketName)
                .executeAsync(new UfileCallback<MultiUploadInfo>() {
                    @Override
                    public void onResponse(MultiUploadInfo response) {
                        progressTimer = new Timer();
                        onProgressTask = new OnProgressTask();
                        progressTimer.scheduleAtFixedRate(onProgressTask, 1000, 1000);
                        
                        List<MultiUploadCallable> callables = new ArrayList<>();
                        List<MultiUploadPartState> results = new ArrayList<>();
                        byte[] buffer = new byte[response.getBlkSize()];
                        int len = 0;
                        int index = 0;
                        FileInputStream fis = null;
                        if (sentLengths == null)
                            sentLengths = new ArrayList<>();
                        else
                            sentLengths.clear();
                        
                        try {
                            fis = new FileInputStream(file);
                            while ((len = fis.read(buffer)) > 0) {
                                callables.add(new MultiUploadCallable(response, Arrays.copyOf(buffer, len), index++));
                                sentLengths.add(new AtomicLong(0l));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (fis != null)
                                FileUtil.close(fis);
                        }
                        
                        try {
                            List<Future<MultiUploadPartState>> futures = fixedThreadPool.invokeAll(callables);
                            for (Future<MultiUploadPartState> future : futures)
                                results.add(future.get());
                            
                            objectApiBuilder.finishMultiUpload(response, results).execute();
                            handler.post(() -> {
                                if (onUploadDialogListener != null)
                                    onUploadDialogListener.onSuccess(UploadFileDialog.this);
                            });
                        } catch (InterruptedException | ExecutionException | UfileClientException | UfileServerException e) {
                            e.printStackTrace();
                            try {
                                BaseResponseBean res = objectApiBuilder.abortMultiUpload(response).execute();
                                handler.post(() -> {
                                    if (onUploadDialogListener != null)
                                        onUploadDialogListener.onFailed(UploadFileDialog.this,
                                                getContext().getString(R.string.str_upload_multipart_failed_has_aborted) + "\n" + (res == null ? "" : res.toString()));
                                });
                            } catch (UfileClientException | UfileServerException e1) {
                                e1.printStackTrace();
                                handler.post(() -> {
                                    if (onUploadDialogListener != null)
                                        onUploadDialogListener.onFailed(UploadFileDialog.this,
                                                getContext().getString(R.string.str_upload_multipart_failed_and_abort_failed) + "\n" + e1.getMessage());
                                });
                            }
                        } finally {
                            if (onProgressTask != null)
                                onProgressTask.cancel();
                            onProgressTask = null;
                            
                            if (progressTimer != null)
                                progressTimer.cancel();
                            progressTimer = null;
                        }
                    }
                    
                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        handler.post(() -> {
                            if (onUploadDialogListener != null)
                                onUploadDialogListener.onFailed(UploadFileDialog.this, response == null ? error.toString() : response.toString());
                        });
                    }
                });
    }
    
    private Timer progressTimer;
    private OnProgressTask onProgressTask;
    
    private class OnProgressTask extends TimerTask {
        @Override
        public void run() {
            handler.post(() -> {
                long written = 0l;
                for (AtomicLong part : sentLengths)
                    written += part.get();
                int progress = (int) (written * 1.f / contentLength * 100);
                dialog_progress.setProgress(progress);
                txt_dialog_progress_info.setText(String.format("%d %%", progress));
            });
        }
    }
    
    private class MultiUploadCallable implements Callable<MultiUploadPartState> {
        private MultiUploadInfo state;
        private byte[] data;
        private int partIndex;
        private final int retryCount = 3;
        
        public MultiUploadCallable(MultiUploadInfo state, byte[] data, int partIndex) {
            this.state = state;
            this.data = data;
            this.partIndex = partIndex;
        }
        
        @Override
        public MultiUploadPartState call() throws UfileServerException, UfileClientException {
            int failedCount = 0;
            while (failedCount < retryCount) {
                try {
                    MultiUploadPartState res = objectApiBuilder.multiUploadPart(state, data, partIndex)
                            /**
                             * 设置是否上传文件MD5校验码，默认为true
                             */
                            .withVerifyMd5(false)
                            /**
                             * 设置自定义进度回调监听，默认每秒回调
                             */
//                            .withProgressConfig(ProgressConfig.callbackWithPercent(1))
                            .setOnProgressListener((bytesWritten, contentLength) -> sentLengths.get(partIndex).set(bytesWritten))
                            .execute();
                    
                    return res;
                } catch (UfileClientException | UfileServerException e) {
                    e.printStackTrace();
                    failedCount++;
                    if (failedCount == 3)
                        throw e;
                }
            }
            
            return null;
        }
    }
    
    public UploadFileDialog setFile(File file) {
        this.file = file;
        contentLength = file.length();
        return this;
    }
    
    public UploadFileDialog setTargetBucketName(String targetBucketName) {
        this.targetBucketName = targetBucketName;
        return this;
    }
    
    public void setOnUploadDialogListener(OnUploadDialogListener onUploadDialogListener) {
        this.onUploadDialogListener = onUploadDialogListener;
    }
    
    @Override
    public void setTitle(int titleId) {
        this.setTitle(getContext().getString(titleId));
    }
    
    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title = title == null ? null : title.toString();
    }
    
    public interface OnUploadDialogListener {
        void onSuccess(Dialog dialog);
        
        void onFailed(Dialog dialog, String errMsg);
    }
    
    public static class Builder {
        private Context context;
        private Integer themeResId;
        private File file;
        private String targetBucketName;
        private ObjectApiBuilder objectApiBuilder;
        private boolean isCancelable, isOutsideTouchCancelable;
        private OnUploadDialogListener onUploadDialogListener;
        
        public Builder(Context context) {
            this.context = context;
        }
        
        public Builder(Context context, int themeResId) {
            this.context = context;
            this.themeResId = themeResId;
        }
        
        public Builder setFile(File file) {
            this.file = file;
            return this;
        }
        
        public Builder setTargetBucketName(String targetBucketName) {
            this.targetBucketName = targetBucketName;
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
        
        public Builder setOnUploadDialogListener(OnUploadDialogListener onUploadDialogListener) {
            this.onUploadDialogListener = onUploadDialogListener;
            return this;
        }
        
        public Builder setObjectApiBuilder(ObjectApiBuilder objectApiBuilder) {
            this.objectApiBuilder = objectApiBuilder;
            return this;
        }
        
        public UploadFileDialog create() {
            UploadFileDialog dialog = themeResId == null ? new UploadFileDialog(context) : new UploadFileDialog(context, themeResId);
            dialog.file = file;
            dialog.contentLength = file.length();
            dialog.targetBucketName = targetBucketName;
            dialog.objectApiBuilder = objectApiBuilder;
            dialog.onUploadDialogListener = onUploadDialogListener;
            dialog.setCanceledOnTouchOutside(isOutsideTouchCancelable);
            dialog.setCancelable(isCancelable);
            
            return dialog;
        }
    }
}
