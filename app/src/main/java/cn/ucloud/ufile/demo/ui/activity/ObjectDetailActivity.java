package cn.ucloud.ufile.demo.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectRemoteAuthorization;
import cn.ucloud.ufile.auth.UfileObjectRemoteAuthorization;
import cn.ucloud.ufile.bean.ObjectInfoBean;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.demo.Constants;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.data.USharedPreferenceHolder;
import cn.ucloud.ufile.demo.ui.dialog.DownloadDialog;
import cn.ucloud.ufile.demo.ui.dialog.ProgressDialog;
import cn.ucloud.ufile.demo.utils.FileUtil;
import cn.ucloud.ufile.demo.utils.PermissionUtil;
import cn.ucloud.ufile.http.UfileCallback;
import okhttp3.Request;

/**
 * Created by joshua on 2019/1/22 18:15.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class ObjectDetailActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQ_CODE_SELECT_DIRECTORY = 0x2000;
    
    private final int REQ_CODE_WRITE_READ_STORAGE = 0x1000;
    
    private USharedPreferenceHolder.USharedPreferences uSharedPreferences;
    
    private TextView txt_object_detail_name, txt_object_detail_bucket, txt_object_detail_mimetype, txt_object_detail_size,
            txt_object_detail_create_time, txt_object_detail_modify_time;
    
    private Button btn_object_download, btn_object_delete;
    
    private ProgressDialog progressDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ObjectInfoBean objectInfo;
    private ObjectAuthorizer authorization;
    private ObjectConfig objectConfig;
    
    private File rootDirectory;
    
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        
        switch (requestCode) {
            case REQ_CODE_WRITE_READ_STORAGE: {
                if (permissions == null || permissions.length == 0)
                    return;
                
                for (int i = 0, len = permissions.length; i < len; i++) {
                    if (TextUtils.equals(permissions[i], Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            rootDirectory = Environment.getExternalStorageDirectory();
                            startActivityForResult(SelectDirectoryActivity.startAction(this, rootDirectory), REQ_CODE_SELECT_DIRECTORY);
                            return;
                        }
                    }
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    @Override
    protected int getContentViewId() {
        return R.layout.activity_object_detail;
    }
    
    @Override
    protected void bindWidget() {
        progressDialog = new ProgressDialog.Builder(this, R.style.DialogNoBgTheme)
                .setMessage(R.string.str_waiting).setCancelable(true).setOutsideTouchCancelable(false).create();
        txt_object_detail_name = findViewById(R.id.txt_object_detail_name);
        txt_object_detail_bucket = findViewById(R.id.txt_object_detail_bucket);
        txt_object_detail_mimetype = findViewById(R.id.txt_object_detail_mimetype);
        txt_object_detail_size = findViewById(R.id.txt_object_detail_size);
        txt_object_detail_create_time = findViewById(R.id.txt_object_detail_create_time);
        txt_object_detail_modify_time = findViewById(R.id.txt_object_detail_modify_time);
        btn_object_download = findViewById(R.id.btn_object_download);
        btn_object_download.setOnClickListener(this);
        btn_object_delete = findViewById(R.id.btn_object_delete);
        btn_object_delete.setOnClickListener(this);
    }
    
    @Override
    protected void initData() {
        uSharedPreferences = USharedPreferenceHolder.getHolder().getSharedPreferences();
        authorization = new UfileObjectRemoteAuthorization(Constants.PUBLIC_KEY, new ObjectRemoteAuthorization.ApiConfig(
                Constants.AUTH_URL, Constants.AUTH_PRIVATE_DOWNLOAD_URL));
        objectConfig = new ObjectConfig(uSharedPreferences.getString(Constants.SpKey.KEY_REGION.name(), ""),
                uSharedPreferences.getString(Constants.SpKey.KEY_PROXY_SUFFIX.name(), ""));
        Intent intent = getIntent();
        if (intent != null)
            objectInfo = (ObjectInfoBean) intent.getSerializableExtra("objectInfo");
    }
    
    @Override
    protected void initUI() {
        if (objectInfo == null) {
            txt_object_detail_name.setText("null");
            txt_object_detail_bucket.setText("null");
            txt_object_detail_mimetype.setText("null");
            txt_object_detail_size.setText("null");
            txt_object_detail_create_time.setText("null");
            txt_object_detail_modify_time.setText("null");
        } else {
            txt_object_detail_name.setText(objectInfo.getFileName());
            txt_object_detail_bucket.setText(objectInfo.getBucketName());
            txt_object_detail_mimetype.setText(objectInfo.getMimeType());
            txt_object_detail_size.setText(FileUtil.formatFileSize(objectInfo.getSize()));
            txt_object_detail_create_time.setText(dateFormat.format(new Date(objectInfo.getCreateTime() * 1000)));
            txt_object_detail_modify_time.setText(dateFormat.format(new Date(objectInfo.getModifyTime() * 1000)));
        }
    }
    
    public static Intent startAction(Context context, ObjectInfoBean objectInfo) {
        Intent intent = new Intent(context, ObjectDetailActivity.class);
        intent.putExtra("objectInfo", objectInfo);
        return intent;
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_object_download: {
                if (PermissionUtil.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    rootDirectory = Environment.getExternalStorageDirectory();
                    startActivityForResult(SelectDirectoryActivity.startAction(this, rootDirectory), REQ_CODE_SELECT_DIRECTORY);
                } else {
                    PermissionUtil.requestPermissions(this, permissions, REQ_CODE_WRITE_READ_STORAGE);
                }
                break;
            }
            case R.id.btn_object_delete: {
                new AlertDialog.Builder(ObjectDetailActivity.this)
                        .setMessage(String.format(getString(R.string.str_delete_confirm), objectInfo.getFileName(), objectInfo.getBucketName()))
                        .setPositiveButton(R.string.str_cancel, (dialog, which) -> dialog.dismiss())
                        .setNegativeButton(R.string.str_sure, (dialog, which) -> {
                            dialog.dismiss();
                            doDelete();
                        })
                        .setCancelable(true)
                        .create().show();
                break;
            }
        }
    }
    
    private void doDelete() {
        progressDialog.show();
        UfileClient.object(authorization, objectConfig).deleteObject(objectInfo.getFileName(), objectInfo.getBucketName())
                .executeAsync(new UfileCallback<BaseResponseBean>() {
                    @Override
                    public void onResponse(BaseResponseBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            if (response.getRetCode() == 0) {
                                Toast.makeText(ObjectDetailActivity.this, R.string.str_delete_success, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                new AlertDialog.Builder(ObjectDetailActivity.this)
                                        .setTitle(R.string.str_error)
                                        .setMessage(response.getMessage())
                                        .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                                        .setCancelable(false)
                                        .create().show();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            new AlertDialog.Builder(ObjectDetailActivity.this)
                                    .setTitle(R.string.str_error)
                                    .setMessage(response == null ? error.toString() : response.getErrMsg())
                                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .create().show();
                        });
                    }
                });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQ_CODE_SELECT_DIRECTORY: {
                if (resultCode != RESULT_OK)
                    return;
                
                File file = (File) data.getSerializableExtra("directory");
                downloadObject(file.getAbsolutePath());
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void downloadObject(String saveDir) {
        progressDialog.show();
        ObjectApiBuilder apiBuilder = UfileClient.object(authorization, objectConfig);
        apiBuilder.objectProfile(objectInfo.getFileName(), objectInfo.getBucketName())
                .executeAsync(new UfileCallback<ObjectProfile>() {
                    @Override
                    public void onResponse(ObjectProfile response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            DownloadDialog downloadDialog = new DownloadDialog.Builder(ObjectDetailActivity.this, R.style.DialogNoBgTheme)
                                    .setCancelable(false)
                                    .setObjectProfile(response)
                                    .setSavePath(saveDir)
                                    .setObjectApiBuilder(apiBuilder)
                                    .setOnDownloadDialogListener(new DownloadDialog.OnDownloadDialogListener() {
                                        @Override
                                        public void onSuccess(Dialog dialog) {
                                            dialog.dismiss();
                                            Toast.makeText(ObjectDetailActivity.this, R.string.str_download_success, Toast.LENGTH_SHORT).show();
                                        }
                                        
                                        @Override
                                        public void onFailed(Dialog dialog, String errMsg) {
                                            dialog.dismiss();
                                            new AlertDialog.Builder(ObjectDetailActivity.this)
                                                    .setTitle(R.string.str_error)
                                                    .setMessage(errMsg)
                                                    .setPositiveButton(R.string.str_got_it, (alertDialog, which) -> alertDialog.dismiss())
                                                    .setCancelable(false)
                                                    .create().show();
                                        }
                                    })
                                    .create();
                            
                            downloadDialog.show();
                        });
                    }
                    
                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            new AlertDialog.Builder(ObjectDetailActivity.this)
                                    .setTitle(R.string.str_error)
                                    .setMessage(response == null ? error.toString() : response.getErrMsg())
                                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .create().show();
                        });
                    }
                });
    }
}
