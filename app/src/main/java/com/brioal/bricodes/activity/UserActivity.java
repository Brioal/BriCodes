package com.brioal.bricodes.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.brioal.bricodes.R;
import com.brioal.bricodes.fragment.LoginFragment;
import com.brioal.bricodes.fragment.RegisterFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.BaseActivity;
import me.imid.swipebacklayout.lib.StatusBarUtils;

public class UserActivity extends BaseActivity {
    private static final String TAG = "UserInfo";
    @Bind(R.id.user_group)
    RadioGroup group;
    @Bind(R.id.user_login)
    RadioButton btn_login;
    @Bind(R.id.user_register)
    RadioButton btn_register;

    private RegisterFragment registerFragment;
    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        loginFragment = new LoginFragment();
        getFragmentManager().beginTransaction().add(R.id.user_container, loginFragment).commit();
        final FragmentManager fragmentManager = getFragmentManager();
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.user_login) {
                    //登陆选中
                    Log.i(TAG, "onCheckedChanged: 选中登陆");
                    btn_login.setTextColor(getResources().getColor(R.color.color_white));
                    btn_register.setTextColor(getResources().getColor(R.color.color_blank_dark));
                    if (loginFragment == null) {
                        loginFragment = new LoginFragment();
                    }

                        fragmentManager.beginTransaction().hide(registerFragment).show(loginFragment).commit();


                } else {
                    Log.i(TAG, "onCheckedChanged: 选中注册");
                    btn_register.setTextColor(getResources().getColor(R.color.color_white));
                    btn_login.setTextColor(getResources().getColor(R.color.color_blank_dark));
                    if (registerFragment == null) {
                        registerFragment = new RegisterFragment();

                    }
                    fragmentManager.beginTransaction().hide(loginFragment).commit();
                    if (registerFragment.isAdded()) {
                        fragmentManager.beginTransaction().show(registerFragment).commit();

                    } else {
                        fragmentManager.beginTransaction().add(R.id.user_container, registerFragment).commit();
                    }


                }
            }
        });
    }


    @Override
    protected void setStatusBar() {
        StatusBarUtils.setTranslucent(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                UserActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
