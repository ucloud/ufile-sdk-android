package cn.ucloud.ufile.demo.ui.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import cn.ucloud.ufile.demo.R;

/**
 * Created by joshua on 2019/1/23 17:09.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class DirectoryView extends LinearLayout {
    private final String TAG = getClass().getSimpleName();
    
    private View rootView;
    private TextView txtDirectoryName;
    
    private int directoryIndex;
    private String directoryName;
    private String absolutePath;
    
    public DirectoryView(Context context, String absolutePath, String directoryName, int index) {
        super(context);
        this.absolutePath = absolutePath;
        this.directoryName = directoryName;
        this.directoryIndex = index;
        init();
    }
    
    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_directory_view, this);
        txtDirectoryName = rootView.findViewById(R.id.txt_directory_name);
        txtDirectoryName.setText(directoryName);
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }
    
    public String getDirectoryName() {
        return directoryName;
    }
    
    public int getDirectoryIndex() {
        return directoryIndex;
    }
}
