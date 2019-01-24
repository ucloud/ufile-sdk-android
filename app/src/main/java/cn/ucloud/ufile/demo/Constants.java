package cn.ucloud.ufile.demo;

/**
 * Created by joshua on 2019/1/10 17:03.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class Constants {
    public enum SpKey {
        KEY_REGION,
        KEY_PROXY_SUFFIX,
        KEY_DEFAULT_BUCKET,
    }
    
    public static final String PUBLIC_KEY = BuildConfig.PUBLIC_KEY;
    public static final String AUTH_URL = BuildConfig.AUTH_URL;
    public static final String AUTH_PRIVATE_DOWNLOAD_URL = BuildConfig.AUTH_PRIVATE_DOWNLOAD_URL;
    public static final String DEFAULT_DOMAIN_PROXY_SUFFIX = "ufileos.com";
}
