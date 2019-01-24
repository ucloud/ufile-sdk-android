package cn.ucloud.ufile.demo.ui.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.ucloud.ufile.demo.R;

/**
 * Created by joshua on 2019/1/14 18:45.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class FileAdapter extends BaseAdapter {
    private List<File> data;
    private Context context;
    private LayoutInflater layoutInflater;
    
    public FileAdapter(Context context) {
        this.data = new ArrayList<>();
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }
    
    @Override
    public File getItem(int position) {
        return data == null ? null : data.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_file_list, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final File file = getItem(position);
        if (file.isDirectory()) {
            holder.img_file_icon.setImageResource(R.drawable.img_directory);
        } else {
            holder.img_file_icon.setImageResource(R.drawable.img_file);
        }
        holder.txt_file_name.setText(file.getName());
        
        return convertView;
    }
    
    private class ViewHolder {
        private AppCompatImageView img_file_icon;
        private TextView txt_file_name;
        
        private ViewHolder(View view) {
            this.img_file_icon = view.findViewById(R.id.img_file_icon);
            this.txt_file_name = view.findViewById(R.id.txt_file_name);
        }
    }
    
    public void refreshData(List<File> data) {
        this.data = data;
        
        notifyDataSetChanged();
    }
}
