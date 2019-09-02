package cn.ucloud.ufile.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

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
public class EditDomainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener {
    private boolean canContinue;
    
    private RadioGroup radio_grp_domain_type;
    private AppCompatRadioButton radio_btn_normal_domain, radio_btn_custom_domain;
    private LinearLayout layout_normal_domain, layout_custom_domain;
    private AppCompatEditText edit_domain_proxy_suffix, edit_custom_domain;
    private AppCompatSpinner spinner_region;
    private Button btn_edit_save, btn_edit_cancel;
    
    private USharedPreferenceHolder.USharedPreferences uSharedPreferences;
    private String customDomain, region, proxySuffix = Constants.DEFAULT_DOMAIN_PROXY_SUFFIX;
    private String[] regionValues;
    private int domainType = Constants.DOMAIN_TYPE_NORMAL;
    
    @Override
    protected int getContentViewId() {
        return R.layout.activity_edit_domain;
    }
    
    @Override
    protected void bindWidget() {
        radio_grp_domain_type = findViewById(R.id.radio_grp_domain_type);
        radio_btn_normal_domain = findViewById(R.id.radio_btn_normal_domain);
        radio_btn_custom_domain = findViewById(R.id.radio_btn_custom_domain);
        layout_normal_domain = findViewById(R.id.layout_normal_domain);
        layout_custom_domain = findViewById(R.id.layout_custom_domain);
        edit_domain_proxy_suffix = findViewById(R.id.edit_domain_proxy_suffix);
        edit_custom_domain = findViewById(R.id.edit_custom_domain);
        spinner_region = findViewById(R.id.spinner_region);
        
        btn_edit_save = findViewById(R.id.btn_edit_save);
        btn_edit_cancel = findViewById(R.id.btn_edit_cancel);
        
        radio_grp_domain_type.setOnCheckedChangeListener(this);
        spinner_region.setOnItemSelectedListener(this);
        
        btn_edit_save.setOnClickListener(this);
        btn_edit_cancel.setOnClickListener(this);
    }
    
    @Override
    protected void initData() {
        uSharedPreferences = USharedPreferenceHolder.getHolder().getSharedPreferences();
        regionValues = getResources().getStringArray(R.array.regions_value);
        domainType = uSharedPreferences.getInt(Constants.SpKey.KEY_DOMAIN_TYPE.name(), Constants.DOMAIN_TYPE_NORMAL);
        region = uSharedPreferences.getString(Constants.SpKey.KEY_REGION.name(), null);
        proxySuffix = uSharedPreferences.getString(Constants.SpKey.KEY_PROXY_SUFFIX.name(), proxySuffix);
        customDomain = uSharedPreferences.getString(Constants.SpKey.KEY_CUSTOM_DOMAIN.name(), "");
        
        Intent intent = getIntent();
        if (intent != null)
            canContinue = intent.getBooleanExtra("canContinue", false);
    }
    
    @Override
    protected void initUI() {
        if (domainType == Constants.DOMAIN_TYPE_NORMAL) {
            radio_grp_domain_type.check(R.id.radio_btn_normal_domain);
            if (TextUtils.isEmpty(region)) {
                spinner_region.setPrompt(getString(R.string.str_region));
            } else {
                for (int i = 0, size = regionValues.length; i < size; i++) {
                    if (TextUtils.equals(regionValues[i], region)) {
                        spinner_region.setSelection(i);
                        break;
                    }
                }
            }
            edit_domain_proxy_suffix.setText(proxySuffix);
        } else {
            radio_grp_domain_type.check(R.id.radio_btn_custom_domain);
            edit_custom_domain.setText(customDomain);
        }
        
        btn_edit_cancel.setText(canContinue ? R.string.str_cancel : R.string.str_quit);
    }
    
    public static Intent startAction(Context context, boolean canContinue) {
        Intent intent = new Intent(context, EditDomainActivity.class);
        intent.putExtra("canContinue", canContinue);
        return intent;
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_save: {
                if (domainType == Constants.DOMAIN_TYPE_NORMAL) {
                    proxySuffix = edit_domain_proxy_suffix.getText().toString().trim();
                    
                    if (TextUtils.isEmpty(proxySuffix)) {
                        new AlertDialog.Builder(EditDomainActivity.this)
                                .setTitle(R.string.str_error)
                                .setMessage(R.string.alert_no_proxy_suffix)
                                .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .setCancelable(false)
                                .show();
                        return;
                    }
                    uSharedPreferences.edit()
                            .putInt(Constants.SpKey.KEY_DOMAIN_TYPE.name(), domainType)
                            .putString(Constants.SpKey.KEY_REGION.name(), region)
                            .putString(Constants.SpKey.KEY_PROXY_SUFFIX.name(), proxySuffix).apply();
                } else {
                    customDomain = edit_custom_domain.getText().toString().trim();
                    if (TextUtils.isEmpty(customDomain)) {
                        new AlertDialog.Builder(EditDomainActivity.this)
                                .setTitle(R.string.str_error)
                                .setMessage(R.string.alert_no_custom_domain)
                                .setPositiveButton(R.string.str_got_it, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .setCancelable(false)
                                .show();
                        return;
                    }
                    uSharedPreferences.edit()
                            .putInt(Constants.SpKey.KEY_DOMAIN_TYPE.name(), domainType)
                            .putString(Constants.SpKey.KEY_CUSTOM_DOMAIN.name(), customDomain).apply();
                }
                
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
        region = regionValues[position];
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_btn_normal_domain: {
                domainType = Constants.DOMAIN_TYPE_NORMAL;
                layout_custom_domain.setVisibility(View.GONE);
                layout_normal_domain.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.radio_btn_custom_domain: {
                domainType = Constants.DOMAIN_TYPE_CUSTOM;
                layout_normal_domain.setVisibility(View.GONE);
                layout_custom_domain.setVisibility(View.VISIBLE);
                break;
            }
        }
    }
}
