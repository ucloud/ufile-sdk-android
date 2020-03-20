package cn.ucloud.ufile.demo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.ucloud.ufile.bean.ObjectInfoBean;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.utils.FileUtil;

/**
 * Created by joshua on 2019/1/14 18:45.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class ObjectAdapter extends BaseAdapter {
    private List<ObjectInfoBean> data;
    private Context context;
    private LayoutInflater layoutInflater;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    
    public ObjectAdapter(Context context, List<ObjectInfoBean> data) {
        this.data = data;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }
    
    @Override
    public ObjectInfoBean getItem(int position) {
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
            convertView = layoutInflater.inflate(R.layout.item_object_list, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final ObjectInfoBean obj = getItem(position);
        if (obj == null) {
            holder.txt_object_name.setText("Null");
            holder.txt_object_mimetype.setText("Null");
            holder.txt_object_size.setText("Null");
            holder.txt_object_modify_time.setText("Null");
        } else {
            holder.txt_object_name.setText(obj.getFileName());
            holder.txt_object_mimetype.setText(obj.getMimeType());
            holder.txt_object_size.setText(FileUtil.formatFileSize(obj.getSize()));
            holder.txt_object_modify_time.setText(dateFormat.format(new Date(obj.getModifyTime() * 1000)));
        }
        
        return convertView;
    }
    
    private class ViewHolder {
        private TextView txt_object_name;
        private TextView txt_object_mimetype;
        private TextView txt_object_size;
        private TextView txt_object_modify_time;
        
        private ViewHolder(View view) {
            this.txt_object_name = view.findViewById(R.id.txt_object_name);
            this.txt_object_mimetype = view.findViewById(R.id.txt_object_mimetype);
            this.txt_object_size = view.findViewById(R.id.txt_object_size);
            this.txt_object_modify_time = view.findViewById(R.id.txt_object_modify_time);
        }
    }
}
