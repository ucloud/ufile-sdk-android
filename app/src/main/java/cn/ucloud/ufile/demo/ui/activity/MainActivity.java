package cn.ucloud.ufile.demo.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.auth.ObjectRemoteAuthorization;
import cn.ucloud.ufile.auth.UfileObjectRemoteAuthorization;
import cn.ucloud.ufile.bean.ObjectInfoBean;
import cn.ucloud.ufile.bean.ObjectListBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.Constants;
import cn.ucloud.ufile.demo.data.USharedPreferenceHolder;
import cn.ucloud.ufile.demo.ui.adapter.ObjectAdapter;
import cn.ucloud.ufile.demo.ui.dialog.InputCharStreamDialog;
import cn.ucloud.ufile.demo.ui.dialog.ProgressDialog;
import cn.ucloud.ufile.demo.ui.dialog.SelectUploadTypeDialog;
import cn.ucloud.ufile.demo.ui.dialog.UploadFileDialog;
import cn.ucloud.ufile.demo.ui.dialog.UploadStreamDialog;
import cn.ucloud.ufile.demo.utils.JLog;
import cn.ucloud.ufile.demo.utils.PermissionUtil;
import cn.ucloud.ufile.http.UfileCallback;
import okhttp3.Request;


public class MainActivity extends BaseActivity
        implements View.OnClickListener, OnRefreshLoadMoreListener, AdapterView.OnItemClickListener {
    private final String TAG = getClass().getSimpleName();
    
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private AppCompatEditText edit_bucket;
    private AppCompatImageButton btn_refresh;
    private TextView txt_current_bucket_name;
    private AppCompatImageButton btn_upload_object;
    
    private ViewGroup layout_object_list;
    private ViewGroup layout_empty_data;
    private SmartRefreshLayout refresh_layout_object_list;
    private ListView object_list_view;
    private ObjectAdapter objectAdapter;
    private List<ObjectInfoBean> data;
    
    private USharedPreferenceHolder.USharedPreferences uSharedPreferences;
    private String bucketName = "";
    private String nextMark = "";
    
    private UfileObjectRemoteAuthorization authorization;
    private ObjectConfig objectConfig;
    private ObjectApiBuilder objectApiBuilder;
    
    private File rootDirectory;
    private ProgressDialog progressDialog;
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQ_CODE_WRITE_READ_STORAGE: {
                if (permissions == null || permissions.length == 0)
                    return;
                
                for (int i = 0, len = permissions.length; i < len; i++) {
                    if (TextUtils.equals(permissions[i], Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            rootDirectory = Environment.getExternalStorageDirectory();
                            needRefresh = false;
                            startActivityForResult(SelectFileActivity.startAction(this, rootDirectory, bucketName), SelectFileActivity.REQ_CODE_SELECT_FILE);
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
        return R.layout.activity_main;
    }
    
    @Override
    protected void bindWidget() {
        progressDialog = new ProgressDialog.Builder(this, R.style.DialogNoBgTheme)
                .setMessage(R.string.str_waiting).setCancelable(true).setOutsideTouchCancelable(false).create();
        edit_bucket = findViewById(R.id.edit_bucket);
        btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(this);
        txt_current_bucket_name = findViewById(R.id.txt_current_bucket_name);
        btn_upload_object = findViewById(R.id.btn_upload_object);
        btn_upload_object.setOnClickListener(this);
        
        layout_object_list = findViewById(R.id.layout_object_list);
        layout_empty_data = findViewById(R.id.layout_empty_data);
        refresh_layout_object_list = findViewById(R.id.refresh_layout_object_list);
        refresh_layout_object_list.setOnRefreshLoadMoreListener(this);
        object_list_view = findViewById(R.id.object_list_view);
        object_list_view.addFooterView(LayoutInflater.from(this).inflate(R.layout.view_empty_footer, null));
        object_list_view.setOnItemClickListener(this);
    }
    
    @Override
    protected void initData() {
        uSharedPreferences = USharedPreferenceHolder.getHolder().getSharedPreferences();
        bucketName = uSharedPreferences.getString(Constants.SpKey.KEY_DEFAULT_BUCKET.name(), bucketName);
        data = new ArrayList<>();
        objectAdapter = new ObjectAdapter(this, data);
        object_list_view.setAdapter(objectAdapter);
    }
    
    @Override
    protected void initUI() {
        if (!checkData())
            return;
        
        edit_bucket.setText(bucketName);
    }
    
    private boolean checkData() {
        authorization = null;
        String publicKey = uSharedPreferences.getString(Constants.SpKey.KEY_PUBLIC_KEY.name(), null);
        if (TextUtils.isEmpty(publicKey)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.str_error)
                    .setMessage(R.string.alert_no_public_key)
                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                        dialog.dismiss();
                        startActivity(EditAuthServerActivity.startAction(MainActivity.this, false));
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            return false;
        }
        
        String authUrl = uSharedPreferences.getString(Constants.SpKey.KEY_APPLY_AUTH_URL.name(), null);
        String authPrivateUrl = uSharedPreferences.getString(Constants.SpKey.KEY_APPLY_PRIVATE_AUTH_URL.name(), null);
        
        if (TextUtils.isEmpty(authUrl) || TextUtils.isEmpty(authPrivateUrl)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.str_error)
                    .setMessage(R.string.alert_no_auth_server_url)
                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                        dialog.dismiss();
                        startActivity(EditAuthServerActivity.startAction(MainActivity.this, false));
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            return false;
        }
        
        int domainType = uSharedPreferences.getInt(Constants.SpKey.KEY_DOMAIN_TYPE.name(), -1);
        if (domainType == -1) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.str_error)
                    .setMessage(R.string.alert_no_domain_type)
                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                        dialog.dismiss();
                        startActivity(EditDomainActivity.startAction(MainActivity.this, false));
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            return false;
        }
        
        authorization = new UfileObjectRemoteAuthorization(publicKey, new ObjectRemoteAuthorization.ApiConfig(authUrl, authPrivateUrl));
        return true;
    }
    
    private boolean needRefresh = true;
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!needRefresh) {
            needRefresh = true;
            return;
        }
        
        if (authorization != null)
            refresh_layout_object_list.autoRefresh();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_auth_server: {
                startActivity(EditAuthServerActivity.startAction(MainActivity.this, true));
                return true;
            }
            case R.id.action_edit_domain_bucket: {
                startActivity(EditDomainActivity.startAction(MainActivity.this, true));
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_refresh: {
                hideSoftInput();
                saveBucketConfig();
                refresh_layout_object_list.autoRefresh();
                break;
            }
            case R.id.btn_upload_object: {
                new SelectUploadTypeDialog(this, R.style.DialogNoBgTheme)
                        .setOnSelectUploadTypeListener((dialog, type) -> {
                            switch (type) {
                                case SelectUploadTypeDialog.OnSelectUploadTypeListener.UPLOAD_TYPE_FILE: {
                                    dialog.dismiss();
                                    if (PermissionUtil.checkPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        rootDirectory = Environment.getExternalStorageDirectory();
                                        needRefresh = false;
                                        startActivityForResult(SelectFileActivity.startAction(MainActivity.this, rootDirectory, bucketName), SelectFileActivity.REQ_CODE_SELECT_FILE);
                                    } else {
                                        PermissionUtil.requestPermissions(MainActivity.this, permissions, Constants.REQ_CODE_WRITE_READ_STORAGE);
                                    }
                                    break;
                                }
                                case SelectUploadTypeDialog.OnSelectUploadTypeListener.UPLOAD_TYPE_STREAM: {
                                    dialog.dismiss();
                                    new InputCharStreamDialog.Builder(MainActivity.this, R.style.DialogNoBgTheme)
                                            .setTitle(R.string.str_please_input)
                                            .setHint(R.string.str_input_what_you_want_to_upload)
                                            .setCancelable(true)
                                            .setOutsideTouchCancelable(false)
                                            .setOnDialogInputListener(new InputCharStreamDialog.OnDialogInputCharStreamListener() {
                                                
                                                @Override
                                                public void onFinish(Dialog dialog, CharSequence title, CharSequence content) {
                                                    dialog.dismiss();
                                                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                                                        new AlertDialog.Builder(MainActivity.this)
                                                                .setTitle(R.string.str_error)
                                                                .setMessage(getString(R.string.alert_stream_title_and_content_can_not_be_empty))
                                                                .setPositiveButton(R.string.str_got_it, (dialog1, which) -> dialog1.dismiss())
                                                                .setCancelable(false)
                                                                .create().show();
                                                        return;
                                                    }
                                                    doUploadStream(title.toString(), content.toString().getBytes(Charset.forName("UTF-8")));
                                                }
                                                
                                                @Override
                                                public void onCancel(Dialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .create().show();
                                    break;
                                }
                            }
                        })
                        .show();
                break;
            }
        }
    }
    
    private UfileCallback<ObjectListBean> refreshObjectListCallback = new UfileCallback<ObjectListBean>() {
        @Override
        public void onResponse(ObjectListBean response) {
            JLog.D(TAG, "onResponse--->" + (response == null ? "null" : response.toString()));
            getHandler().post(() -> {
                refresh_layout_object_list.finishRefresh();
                data.clear();
                if (response == null) {
                    layout_object_list.setVisibility(View.GONE);
                    layout_empty_data.setVisibility(View.VISIBLE);
                    btn_upload_object.setVisibility(View.GONE);
                } else {
                    nextMark = response.getNextMarker();
                    layout_empty_data.setVisibility(View.GONE);
                    layout_object_list.setVisibility(View.VISIBLE);
                    refresh_layout_object_list.setEnableLoadMore(!TextUtils.isEmpty(response.getNextMarker()));
                    txt_current_bucket_name.setText(response.getBucketName());
                    data.addAll(response.getObjectList());
                    btn_upload_object.setVisibility(View.VISIBLE);
                }
                objectAdapter.notifyDataSetChanged();
            });
        }
        
        @Override
        public void onError(Request request, ApiError error, UfileErrorBean response) {
            JLog.E(TAG, "onError--->" + error.toString()
                    + (response == null ? "" : "\n" + response.toString()));
            getHandler().post(() -> {
                btn_upload_object.setVisibility(View.GONE);
                data.clear();
                objectAdapter.notifyDataSetChanged();
                refresh_layout_object_list.setEnableLoadMore(false);
                refresh_layout_object_list.finishRefresh();
                layout_object_list.setVisibility(View.GONE);
                layout_empty_data.setVisibility(View.VISIBLE);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.str_error)
                        .setMessage(response == null ? error.toString() : response.toString())
                        .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .create().show();
            });
        }
    };
    
    private void saveBucketConfig() {
        bucketName = edit_bucket.getText().toString().trim();
        uSharedPreferences.edit().putString(Constants.SpKey.KEY_DEFAULT_BUCKET.name(), bucketName).apply();
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (objectAdapter == null)
            return;
        
        ObjectInfoBean objectInfo = objectAdapter.getItem(position);
        startActivity(ObjectDetailActivity.startAction(MainActivity.this, objectInfo));
    }
    
    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        JLog.E(TAG, "onLoadMore--->");
        objectApiBuilder.objectList(bucketName).withMarker(nextMark)
                .executeAsync(new UfileCallback<ObjectListBean>() {
                    @Override
                    public void onResponse(ObjectListBean response) {
                        JLog.D(TAG, "onResponse--->" + (response == null ? "null" : response.toString()));
                        getHandler().post(() -> {
                            refresh_layout_object_list.finishLoadMore();
                            if (response != null) {
                                nextMark = response.getNextMarker();
                                refresh_layout_object_list.setEnableLoadMore(!TextUtils.isEmpty(response.getNextMarker()));
                                data.addAll(response.getObjectList());
                                objectAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        JLog.E(TAG, "onError--->" + error.toString()
                                + (response == null ? "" : "\n" + response.toString()));
                        getHandler().post(() -> {
                            refresh_layout_object_list.finishLoadMore();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.str_error)
                                    .setMessage(response == null ? error.toString() : response.toString())
                                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .create().show();
                        });
                    }
                });
    }
    
    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        if (TextUtils.isEmpty(bucketName)) {
            refresh_layout_object_list.finishRefresh();
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.str_error)
                    .setMessage(R.string.alert_input_bucket)
                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .create().show();
            return;
        }
        
        int domainType = uSharedPreferences.getInt(Constants.SpKey.KEY_DOMAIN_TYPE.name(), Constants.DOMAIN_TYPE_NORMAL);
        if (domainType == Constants.DOMAIN_TYPE_NORMAL) {
            objectConfig = new ObjectConfig(uSharedPreferences.getString(Constants.SpKey.KEY_REGION.name(), null),
                    uSharedPreferences.getString(Constants.SpKey.KEY_PROXY_SUFFIX.name(), Constants.DEFAULT_DOMAIN_PROXY_SUFFIX));
        } else {
            objectConfig = new ObjectConfig(uSharedPreferences.getString(Constants.SpKey.KEY_CUSTOM_DOMAIN.name(), null));
        }
        
        nextMark = "";
        
        objectApiBuilder = UfileClient.object(authorization, objectConfig);
        
        objectApiBuilder.objectList(bucketName).withMarker(nextMark)
                .executeAsync(refreshObjectListCallback);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SelectFileActivity.REQ_CODE_SELECT_FILE: {
                if (resultCode != RESULT_OK)
                    return;
                
                File file = (File) data.getSerializableExtra("file");
                doUploadFile(file);
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void doUploadFile(File file) {
        progressDialog.show();
        objectApiBuilder.uploadHit(file).nameAs(file.getName()).toBucket(bucketName)
                .executeAsync(new UfileCallback<BaseResponseBean>() {
                    @Override
                    public void onResponse(BaseResponseBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            if (response == null || response.getRetCode() != 0) {
                                new UploadFileDialog.Builder(MainActivity.this)
                                        .setCancelable(false)
                                        .setFile(file)
                                        .setObjectApiBuilder(objectApiBuilder)
                                        .setTargetBucketName(bucketName)
                                        .setOnUploadDialogListener(new UploadFileDialog.OnUploadDialogListener() {
                                            @Override
                                            public void onSuccess(Dialog dialog) {
                                                dialog.dismiss();
                                                Toast.makeText(MainActivity.this, R.string.str_upload_success, Toast.LENGTH_SHORT).show();
                                            }
                                            
                                            @Override
                                            public void onFailed(Dialog dialog, String errMsg) {
                                                dialog.dismiss();
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle(R.string.str_error)
                                                        .setMessage(errMsg)
                                                        .setPositiveButton(R.string.str_got_it, (dialog1, which) -> dialog1.dismiss())
                                                        .setCancelable(false)
                                                        .create().show();
                                            }
                                        }).create().show();
                                return;
                            }
                            
                            refresh_layout_object_list.autoRefresh();
                            Toast.makeText(MainActivity.this, R.string.str_upload_hit_success, Toast.LENGTH_SHORT).show();
                        });
                    }
                    
                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            if (response != null && response.getResponseCode() == 404) {
                                new UploadFileDialog.Builder(MainActivity.this)
                                        .setCancelable(false)
                                        .setFile(file)
                                        .setObjectApiBuilder(objectApiBuilder)
                                        .setTargetBucketName(bucketName)
                                        .setOnUploadDialogListener(new UploadFileDialog.OnUploadDialogListener() {
                                            @Override
                                            public void onSuccess(Dialog dialog) {
                                                dialog.dismiss();
                                                refresh_layout_object_list.autoRefresh();
                                                Toast.makeText(MainActivity.this, R.string.str_upload_success, Toast.LENGTH_SHORT).show();
                                            }
                                            
                                            @Override
                                            public void onFailed(Dialog dialog, String errMsg) {
                                                dialog.dismiss();
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle(R.string.str_error)
                                                        .setMessage(errMsg)
                                                        .setPositiveButton(R.string.str_got_it, (dialog1, which) -> dialog1.dismiss())
                                                        .setCancelable(false)
                                                        .create().show();
                                            }
                                        }).create().show();
                                return;
                            }
                            
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.str_error)
                                    .setMessage(response == null ? error.toString() : response.toString())
                                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .create().show();
                        });
                    }
                });
    }
    
    private void doUploadStream(String keyName, byte[] bytes) {
        progressDialog.show();
        objectApiBuilder.uploadHit(new ByteArrayInputStream(bytes)).nameAs(keyName).toBucket(bucketName)
                .executeAsync(new UfileCallback<BaseResponseBean>() {
                    @Override
                    public void onResponse(BaseResponseBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            if (response == null || response.getRetCode() != 0) {
                                new UploadStreamDialog.Builder(MainActivity.this)
                                        .setCancelable(false)
                                        .setStream(new ByteArrayInputStream(bytes))
                                        .setStreamKeyName(keyName)
                                        .setObjectApiBuilder(objectApiBuilder)
                                        .setTargetBucketName(bucketName)
                                        .setOnUploadDialogListener(new UploadStreamDialog.OnUploadDialogListener() {
                                            @Override
                                            public void onSuccess(Dialog dialog) {
                                                dialog.dismiss();
                                                Toast.makeText(MainActivity.this, R.string.str_upload_success, Toast.LENGTH_SHORT).show();
                                            }
                                            
                                            @Override
                                            public void onFailed(Dialog dialog, String errMsg) {
                                                dialog.dismiss();
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle(R.string.str_error)
                                                        .setMessage(errMsg)
                                                        .setPositiveButton(R.string.str_got_it, (dialog1, which) -> dialog1.dismiss())
                                                        .setCancelable(false)
                                                        .create().show();
                                            }
                                        }).create().show();
                                return;
                            }
                            
                            refresh_layout_object_list.autoRefresh();
                            Toast.makeText(MainActivity.this, R.string.str_upload_hit_success, Toast.LENGTH_SHORT).show();
                        });
                    }
                    
                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        getHandler().post(() -> {
                            progressDialog.dismiss();
                            if (response != null && response.getResponseCode() == 404) {
                                new UploadStreamDialog.Builder(MainActivity.this)
                                        .setCancelable(false)
                                        .setStream(new ByteArrayInputStream(bytes))
                                        .setStreamKeyName(keyName)
                                        .setObjectApiBuilder(objectApiBuilder)
                                        .setTargetBucketName(bucketName)
                                        .setOnUploadDialogListener(new UploadStreamDialog.OnUploadDialogListener() {
                                            @Override
                                            public void onSuccess(Dialog dialog) {
                                                dialog.dismiss();
                                                refresh_layout_object_list.autoRefresh();
                                                Toast.makeText(MainActivity.this, R.string.str_upload_success, Toast.LENGTH_SHORT).show();
                                            }
                                            
                                            @Override
                                            public void onFailed(Dialog dialog, String errMsg) {
                                                dialog.dismiss();
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle(R.string.str_error)
                                                        .setMessage(errMsg)
                                                        .setPositiveButton(R.string.str_got_it, (dialog1, which) -> dialog1.dismiss())
                                                        .setCancelable(false)
                                                        .create().show();
                                            }
                                        }).create().show();
                                return;
                            }
                            
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.str_error)
                                    .setMessage(response == null ? error.toString() : response.toString())
                                    .setPositiveButton(R.string.str_got_it, (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .create().show();
                        });
                    }
                });
    }
    
}
