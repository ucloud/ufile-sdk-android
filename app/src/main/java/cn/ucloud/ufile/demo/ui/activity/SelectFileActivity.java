package cn.ucloud.ufile.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.ucloud.ufile.demo.Constants;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.data.USharedPreferenceHolder;
import cn.ucloud.ufile.demo.ui.adapter.FileAdapter;
import cn.ucloud.ufile.demo.ui.widgets.DirectoryView;
import cn.ucloud.ufile.demo.ui.widgets.PathScrollView;
import cn.ucloud.ufile.demo.utils.JLog;

/**
 * Created by joshua on 2019/1/23 17:31.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class SelectFileActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static final int REQ_CODE_SELECT_FILE = 0x2001;
    
    private PathScrollView scroll_view_directory_path;
    private ListView list_file_system;
    
    private USharedPreferenceHolder.USharedPreferences uSharedPreferences;
    private File rootDirectory, currentDirectory;
    private List<DirectoryView> pathViewList;
    private FileAdapter adapter;
    private String bucketName;
    
    @Override
    protected int getContentViewId() {
        return R.layout.activity_select_file;
    }
    
    @Override
    protected void bindWidget() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        scroll_view_directory_path = findViewById(R.id.scroll_view_directory_path);
        list_file_system = findViewById(R.id.list_file_system);
        list_file_system.addFooterView(LayoutInflater.from(this).inflate(R.layout.view_empty_footer, null));
        adapter = new FileAdapter(this);
        list_file_system.setAdapter(adapter);
        list_file_system.setOnItemClickListener(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                setResult(RESULT_CANCELED);
                finish();
                return false;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void initData() {
        uSharedPreferences = USharedPreferenceHolder.getHolder().getSharedPreferences();
        Intent intent = getIntent();
        if (intent != null) {
            rootDirectory = (File) intent.getSerializableExtra("rootDirectory");
            bucketName = intent.getStringExtra("bucketName");
        } else {
            rootDirectory = Environment.getExternalStorageDirectory();
            bucketName = "";
        }
        
        pathViewList = new ArrayList<>();
        
        if (rootDirectory == null || !rootDirectory.exists() || !rootDirectory.isDirectory()) {
            Toast.makeText(this, getString(R.string.str_file_illegal), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        
        pathViewList.add(new DirectoryView(this, rootDirectory.getAbsolutePath(), "root", 0));
        
        String latestDirectory = uSharedPreferences.getString(Constants.SpKey.KEY_LATEST_UPLOAD_DIRECTORY.name(), "");
        
        if (TextUtils.isEmpty(latestDirectory)) {
            currentDirectory = rootDirectory;
        } else {
            currentDirectory = new File(latestDirectory);
            if (currentDirectory == null || !currentDirectory.exists() || !currentDirectory.isDirectory()) {
                currentDirectory = rootDirectory;
            } else {
                if (!latestDirectory.startsWith(rootDirectory.getAbsolutePath())) {
                    currentDirectory = rootDirectory;
                } else {
                    JLog.T(TAG, "[rootDirectory]:" + rootDirectory);
                    latestDirectory = latestDirectory.substring(rootDirectory.getAbsolutePath().length());
                    JLog.T(TAG, "[latestDirectory]:" + latestDirectory);
                    String[] nodes = latestDirectory.split(File.separator);
                    String rootPath = rootDirectory.getAbsolutePath();
                    if (!rootPath.endsWith(File.separator))
                        rootPath += File.separator;
                    JLog.T(TAG, "[rootPath]:" + rootPath);
                    StringBuffer sb = new StringBuffer(rootPath);
                    int startLen = pathViewList.size();
                    for (String node : nodes) {
                        JLog.T(TAG, "[node]:" + node);
                        if (TextUtils.isEmpty(node))
                            continue;
                        sb.append(node + File.separator);
                        pathViewList.add(new DirectoryView(this, sb.toString(), node, startLen++));
                    }
                }
            }
        }
    }
    
    @Override
    protected void initUI() {
        refreshList(currentDirectory);
        for (DirectoryView view : pathViewList) {
            view.setOnClickListener(this);
            scroll_view_directory_path.addView(view, view.getDirectoryIndex());
        }
    }
    
    public static Intent startAction(Context context, File rootDirectory, String bucketName) {
        Intent intent = new Intent(context, SelectFileActivity.class);
        intent.putExtra("rootDirectory", rootDirectory);
        intent.putExtra("bucketName", bucketName);
        return intent;
    }
    
    @Override
    public void onClick(View v) {
        if (v instanceof DirectoryView) {
            DirectoryView dv = (DirectoryView) v;
            int index = dv.getDirectoryIndex();
            List<DirectoryView> deleteList = new ArrayList<>();
            for (int i = index + 1, len = pathViewList.size(); i < len; i++) {
                deleteList.add(pathViewList.get(i));
            }
            for (DirectoryView del : deleteList)
                scroll_view_directory_path.removeView(del);
            
            pathViewList.removeAll(deleteList);
            
            refreshList(new File(dv.getAbsolutePath()));
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = adapter.getItem(position);
        if (file.isDirectory()) {
            DirectoryView dv = new DirectoryView(this, file.getAbsolutePath(), file.getName(), pathViewList.size());
            dv.setOnClickListener(this);
            pathViewList.add(dv);
            scroll_view_directory_path.addView(dv, dv.getDirectoryIndex());
            refreshList(file);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(String.format(getString(R.string.str_is_confirm_upload_file), file.getName(), bucketName))
                    .setPositiveButton(R.string.str_sure, (dialog, which) -> {
                        dialog.dismiss();
                        uSharedPreferences.edit().putString(Constants.SpKey.KEY_LATEST_UPLOAD_DIRECTORY.name(), currentDirectory.getAbsolutePath()).apply();
                        Intent result = new Intent();
                        result.putExtra("file", file);
                        setResult(RESULT_OK, result);
                        finish();
                    })
                    .setNegativeButton(R.string.str_cancel, (dialog, which) -> dialog.dismiss()).show();
        }
    }
    
    private void refreshList(File file) {
        currentDirectory = file;
        List<File> fileList = new ArrayList<>();
        File[] fileArr = currentDirectory.listFiles();
        for (File f : fileArr)
            fileList.add(f);
        
        Collections.sort(fileList, fileComparator);
        adapter.refreshData(fileList);
    }
    
    private Comparator<File> fileComparator = (f1, f2) -> {
        if (f1.isDirectory() ^ f2.isDirectory()) {
            if (f1.isDirectory())
                return -1;
            
            return 0;
        } else {
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    };
    
    @Override
    public void onBackPressed() {
        int len = pathViewList.size();
        if (len > 1) {
            pathViewList.get(len - 2).performClick();
            return;
        }
        super.onBackPressed();
    }
}
