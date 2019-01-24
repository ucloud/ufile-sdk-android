package cn.ucloud.ufile.demo;

import android.app.Application;

import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;

/**
 * Created by joshua on 2019/1/16 18:25.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class UfileApplication extends Application {
    private String TAG = getClass().getSimpleName();
    
    private static UfileApplication mApp = null;
    
    public UfileApplication() {
        mApp = this;
    }
    
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            return new MaterialHeader(context).setColorSchemeColors(getApp().getResources().getColor(R.color.colorPrimary));
            //.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> {
            //指定为经典Footer，默认是 BallPulseFooter
            return new ClassicsFooter(context).setAccentColorId(R.color.dark_gray).setPrimaryColorId(R.color.gray);
        });
    }
    
    
    public static UfileApplication getApp() {
        return mApp;
    }
}