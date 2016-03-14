package com.brioal.bricodes.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brioal.bricodes.MainActivity;
import com.brioal.bricodes.R;
import com.brioal.bricodes.base.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginInfo";
    @Bind(R.id.login_email)
    EditText loginEmail;
    @Bind(R.id.login_pass)
    EditText loginPass;
    @Bind(R.id.login_btn_login)
    Button loginBtnLogin;
    @Bind(R.id.login_tv_intent)
    TextView tv_msg;
    private String userEmail;
    private String userPass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        setView();

        return view;
    }

    public void setView() {
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
            userPass = getArguments().getString("userPass");
            loginEmail.setText(userEmail);
            loginPass.setText(userPass);

        }
        loginBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobUser.loginByAccount(getActivity(), loginEmail.getText().toString(), loginPass.getText().toString(), new LogInListener<User>() {

                    @Override
                    public void done(User user, BmobException e) {
                        if (user != null) {
                            loginBtnLogin.setText("登陆成功，正在跳转...");
                            getActivity().finish();
                            getActivity().overridePendingTransition(0, R.anim.zoom_exit);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        } else {
                            loginBtnLogin.setText("登陆失败，请确认信息后重试");
                            Log.i(TAG, "onFailure: " + e.toString());
                        }
                    }
                });
            }
        });

        //点击跳转到注册
        String text1 = "还没有账号？点击此处注册";
        SpannableString spannableString1 = new SpannableString(text1);

        spannableString1.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                RegisterFragment fragment = new RegisterFragment();
                transaction.add(R.id.user_container, fragment);
                transaction.hide(LoginFragment.this);
                transaction.commit();

            }
        }, 6, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_msg.setText(spannableString1);
        tv_msg.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
