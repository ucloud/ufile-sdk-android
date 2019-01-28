package cn.ucloud.ufile.demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Created by joshua on 2019/1/10 16:53.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class USharedPreferenceHolder {
    private final String SUB_SHARED_PREFERENCE_NAME = "ufile_demo_data";
    private WeakReference<USharedPreferences> mWeakRefSP;
    private static volatile USharedPreferenceHolder mHolder = null;
    
    private Context context;
    
    private USharedPreferenceHolder(Context context) {
        this.context = context;
    }
    
    public static USharedPreferenceHolder createHolder(@NonNull Context context) {
        synchronized (USharedPreferenceHolder.class) {
            if (mHolder == null)
                mHolder = new USharedPreferenceHolder(context);
        }
        
        return mHolder;
    }
    
    public synchronized static USharedPreferenceHolder getHolder() {
        return mHolder;
    }
    
    public synchronized USharedPreferences getSharedPreferences() {
        if (mWeakRefSP == null || mWeakRefSP.get() == null)
            mWeakRefSP = new WeakReference<>(new USharedPreferences(context, context.getPackageName() + "_" + SUB_SHARED_PREFERENCE_NAME));
        
        return mWeakRefSP.get();
    }
    
    public static class USharedPreferences {
        private SharedPreferences mSharedPreferences;
        
        private USharedPreferences(@NonNull Context context, @NonNull String name) {
            mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
        
        public SharedPreferences.Editor edit() {
            return mSharedPreferences.edit();
        }
        
        public int getInt(@NonNull String key, int def) {
            return mSharedPreferences.getInt(key, def);
        }
        
        public float getFloat(@NonNull String key, float def) {
            return mSharedPreferences.getFloat(key, def);
        }
        
        public long getLong(@NonNull String key, long def) {
            return mSharedPreferences.getLong(key, def);
        }
        
        public boolean getBoolean(@NonNull String key, boolean def) {
            return mSharedPreferences.getBoolean(key, def);
        }
        
        public String getString(@NonNull String key, String def) {
            return mSharedPreferences.getString(key, def);
        }
        
        public Set<String> getStringSet(@NonNull String key, Set<String> def) {
            return mSharedPreferences.getStringSet(key, def);
        }
    }
}
