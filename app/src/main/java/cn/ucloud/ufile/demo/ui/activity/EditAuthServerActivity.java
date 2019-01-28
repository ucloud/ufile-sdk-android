package cn.ucloud.ufile.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.ucloud.ufile.demo.Constants;
import cn.ucloud.ufile.demo.R;
import cn.ucloud.ufile.demo.data.USharedPreferenceHolder;
import cn.ucloud.ufile.demo.utils.JLog;

/**
 * Created by joshua on 2019/1/28 15:54.
 * Company: UCloud
 * E-mail: joshua.yin@ucloud.cn
 */
public class EditAuthServerActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private boolean canContinue;
    
    private AppCompatEditText edit_public_key, edit_apply_auth_server, edit_apply_private_download_url_auth_server;
    private AppCompatSpinner spinner_auth_url, spinner_auth_private_url;
    private Button btn_edit_save, btn_edit_cancel;
    
    private USharedPreferenceHolder.USharedPreferences uSharedPreferences;
    private String[] httpProtocol;
    private String applyAuthHttpProtocol, applyPrivateAuthHttpProtocol;
    private final String applyAuthSuffix = "/applyAuth", applyPrivateAuthSuffix = "/applyPrivateUrlAuth";
    private final String MATCH_DOMAIN_IP = "(?<=://).*(?=/apply?)";
    
    @Override
    protected int getContentViewId() {
        return R.layout.activity_edit_auth_server;
    }
    
    @Override
    protected void bindWidget() {
        edit_public_key = findViewById(R.id.edit_public_key);
        edit_apply_auth_server = findViewById(R.id.edit_apply_auth_server);
        edit_apply_private_download_url_auth_server = findViewById(R.id.edit_apply_private_download_url_auth_server);
        spinner_auth_url = findViewById(R.id.spinner_auth_url);
        spinner_auth_private_url = findViewById(R.id.spinner_auth_private_url);
        btn_edit_save = findViewById(R.id.btn_edit_save);
        btn_edit_cancel = findViewById(R.id.btn_edit_cancel);
        
        spinner_auth_url.setOnItemSelectedListener(this);
        spinner_auth_private_url.setOnItemSelectedListener(this);
        
        btn_edit_save.setOnClickListener(this);
        btn_edit_cancel.setOnClickListener(this);
    }
    
    @Override
    protected void initData() {
        uSharedPreferences = USharedPreferenceHolder.getHolder().getSharedPreferences();
        httpProtocol = getResources().getStringArray(R.array.http_protocol);
        
        Intent intent = getIntent();
        if (intent != null)
            canContinue = intent.getBooleanExtra("canContinue", false);
    }
    
    @Override
    protected void initUI() {
        String publicKey = uSharedPreferences.getString(Constants.SpKey.KEY_PUBLIC_KEY.name(), null);
        String applyAuth = uSharedPreferences.getString(Constants.SpKey.KEY_APPLY_AUTH_URL.name(), null);
        String applyPrivateAuth = uSharedPreferences.getString(Constants.SpKey.KEY_APPLY_PRIVATE_AUTH_URL.name(), null);
    
        applyAuthHttpProtocol = TextUtils.isEmpty(applyAuth) ? "http://" : (applyAuth.startsWith("https://") ? "https://" : "http://");
        applyPrivateAuthHttpProtocol = TextUtils.isEmpty(applyPrivateAuth) ? "http://" : (applyPrivateAuth.startsWith("https://") ? "https://" : "http://");
    
        spinner_auth_url.setSelection(TextUtils.equals(applyAuthHttpProtocol, "https://") ? 1 : 0);
        spinner_auth_private_url.setSelection(TextUtils.equals(applyPrivateAuthHttpProtocol, "https://") ? 1 : 0);
        
        Pattern pattern = Pattern.compile(MATCH_DOMAIN_IP);
        if (!TextUtils.isEmpty(applyAuth)) {
            Matcher matcherApplyAuth = pattern.matcher(applyAuth);
            if (matcherApplyAuth.find())
                applyAuth = matcherApplyAuth.group();
        }
    
        if (!TextUtils.isEmpty(applyPrivateAuth)) {
            Matcher matcherApplyPrivateAuth = pattern.matcher(applyPrivateAuth);
            if (matcherApplyPrivateAuth.find())
                applyPrivateAuth = matcherApplyPrivateAuth.group();
        }
        
        JLog.E(TAG, "[applyAuth]:" + applyAuth + " [applyPrivateAuth]:" + applyPrivateAuth);
        
        edit_public_key.setText(publicKey);
        edit_apply_auth_server.setText(applyAuth);
        edit_apply_private_download_url_auth_server.setText(applyPrivateAuth);
        btn_edit_cancel.setText(canContinue ? R.string.str_cancel : R.string.str_quit);
    }
    
    public static Intent startAction(Context context, boolean canContinue) {
        Intent intent = new Intent(context, EditAuthServerActivity.class);
        intent.putExtra("canContinue", canContinue);
        return intent;
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_save: {
                String publicKey = edit_public_key.getText().toString().trim();
                String applyAuth = edit_apply_auth_server.getText().toString().trim();
                String applyPrivateAuth = edit_apply_private_download_url_auth_server.getText().toString().trim();
                
                if (TextUtils.isEmpty(publicKey)) {
                    new AlertDialog.Builder(EditAuthServerActivity.this)
                            .setTitle(R.string.str_error)
                            .setMessage(R.string.alert_no_public_key)
                            .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(applyAuth) || TextUtils.isEmpty(applyPrivateAuth)) {
                    new AlertDialog.Builder(EditAuthServerActivity.this)
                            .setTitle(R.string.str_error)
                            .setMessage(R.string.alert_no_auth_server_url)
                            .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();
                    return;
                }
                
                uSharedPreferences.edit().putString(Constants.SpKey.KEY_PUBLIC_KEY.name(), publicKey)
                        .putString(Constants.SpKey.KEY_APPLY_AUTH_URL.name(), applyAuthHttpProtocol + applyAuth + applyAuthSuffix)
                        .putString(Constants.SpKey.KEY_APPLY_PRIVATE_AUTH_URL.name(), applyPrivateAuthHttpProtocol + applyPrivateAuth + applyPrivateAuthSuffix)
                        .commit();
                
                startActivity(new Intent(this, MainActivity.class));
                finish();
                
                break;
            }
            case R.id.btn_edit_cancel: {
                if (canContinue)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            }
        }
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_auth_url: {
                applyAuthHttpProtocol = httpProtocol[position];
                break;
            }
            case R.id.spinner_auth_private_url: {
                applyPrivateAuthHttpProtocol = httpProtocol[position];
                break;
            }
        }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    
    }
}
