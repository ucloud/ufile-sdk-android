package cn.ucloud.ufile.demo;

/**
 * Created by joshua on 2019/1/10 17:03.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class Constants {
    public enum SpKey {
        KEY_PUBLIC_KEY,
        KEY_APPLY_AUTH_URL,
        KEY_APPLY_PRIVATE_AUTH_URL,
        KEY_REGION,
        KEY_PROXY_SUFFIX,
        KEY_DEFAULT_BUCKET,
        KEY_LATEST_DOWNLOAD_DIRECTORY,
        KEY_LATEST_UPLOAD_DIRECTORY,
        KEY_DOMAIN_TYPE,
        KEY_CUSTOM_DOMAIN,
    }
    
    public final static int DOMAIN_TYPE_NORMAL = 0;
    public final static int DOMAIN_TYPE_CUSTOM = 1;
    
    public static final String DEFAULT_DOMAIN_PROXY_SUFFIX = "ufileos.com";
    
    public static final int REQ_CODE_WRITE_READ_STORAGE = 0x1000;
}
