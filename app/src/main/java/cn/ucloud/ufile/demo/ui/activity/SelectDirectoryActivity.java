package cn.ucloud.ufile.demo.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
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
import cn.ucloud.ufile.demo.ui.dialog.InputDialog;
import cn.ucloud.ufile.demo.ui.widgets.DirectoryView;
import cn.ucloud.ufile.demo.ui.widgets.PathScrollView;
import cn.ucloud.ufile.demo.utils.FileUtil;
import cn.ucloud.ufile.demo.utils.JLog;

/**
 * Created by joshua on 2019/1/23 17:31.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class SelectDirectoryActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private PathScrollView scroll_view_directory_path;
    private ListView list_file_system;
    
    private USharedPreferenceHolder.USharedPreferences uSharedPreferences;
    private File rootDirectory, currentDirectory;
    private List<DirectoryView> pathViewList;
    private FileAdapter adapter;
    
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    @Override
    protected int getContentViewId() {
        return R.layout.activity_select_directory;
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
        
        findViewById(R.id.btn_current_directory).setOnClickListener(this);
        findViewById(R.id.btn_make_directory).setOnClickListener(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                setResult(RESULT_CANCELED);
                finishActivity(ObjectDetailActivity.REQ_CODE_SELECT_DIRECTORY);
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
        if (intent != null)
            rootDirectory = (File) intent.getSerializableExtra("rootDirectory");
        else
            rootDirectory = Environment.getExternalStorageDirectory();
        
        pathViewList = new ArrayList<>();
        
        if (rootDirectory == null || !rootDirectory.exists() || !rootDirectory.isDirectory()) {
            Toast.makeText(this, getString(R.string.str_file_illegal), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finishActivity(ObjectDetailActivity.REQ_CODE_SELECT_DIRECTORY);
            return;
        }
        
        pathViewList.add(new DirectoryView(this, rootDirectory.getAbsolutePath(), "root", 0));
        
        String latestDirectory = uSharedPreferences.getString(Constants.SpKey.KEY_LATEST_DOWNLOAD_DIRECTORY.name(), "");
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
    
    public static Intent startAction(Context context, File rootDirectory) {
        Intent intent = new Intent(context, SelectDirectoryActivity.class);
        intent.putExtra("rootDirectory", rootDirectory);
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
        } else {
            switch (v.getId()) {
                case R.id.btn_current_directory: {
                    uSharedPreferences.edit().putString(Constants.SpKey.KEY_LATEST_DOWNLOAD_DIRECTORY.name(), currentDirectory.getAbsolutePath()).apply();
                    Intent result = new Intent();
                    result.putExtra("directory", currentDirectory);
                    setResult(RESULT_OK, result);
                    finish();
                    break;
                }
                case R.id.btn_make_directory: {
                    InputDialog dialog = new InputDialog.Builder(this, R.style.DialogNoBgTheme)
                            .setTitle(R.string.str_make_directory)
                            .setDefaultContent(R.string.str_directory)
                            .setCancelable(true)
                            .setOnDialogInputListener(new InputDialog.OnDialogInputListener() {
                                @Override
                                public void onFinish(Dialog dialog, CharSequence content) {
                                    dialog.dismiss();
                                    File tmp = new File(currentDirectory.getAbsolutePath() + File.separator + content);
                                    if (tmp.exists() && tmp.isDirectory()) {
                                        Toast.makeText(SelectDirectoryActivity.this,
                                                String.format(getString(R.string.str_directory_exists), content), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    
                                    if (!tmp.mkdirs()) {
                                        Toast.makeText(SelectDirectoryActivity.this, R.string.str_directory_failed, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    
                                    DirectoryView dv = new DirectoryView(SelectDirectoryActivity.this, tmp.getAbsolutePath(), tmp.getName(), pathViewList.size());
                                    dv.setOnClickListener(SelectDirectoryActivity.this);
                                    pathViewList.add(dv);
                                    scroll_view_directory_path.addView(dv, dv.getDirectoryIndex());
                                    refreshList(tmp);
                                }
                                
                                @Override
                                public void onCancel(Dialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                    break;
                }
            }
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = adapter.getItem(position);
        if (!file.isDirectory())
            return;
        
        DirectoryView dv = new DirectoryView(this, file.getAbsolutePath(), file.getName(), pathViewList.size());
        dv.setOnClickListener(this);
        pathViewList.add(dv);
        scroll_view_directory_path.addView(dv, dv.getDirectoryIndex());
        refreshList(file);
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
