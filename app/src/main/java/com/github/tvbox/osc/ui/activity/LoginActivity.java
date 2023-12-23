package com.github.tvbox.osc.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.beanry.ReUserBean;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author 茶茶
 * @date :2023/12/19
 * @description:
 * 登录注册 茶茶QQ205888578
 */

public class LoginActivity extends AppCompatActivity {

    private TextView user_name_et;
    private TextView user_pass_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //沉浸状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //销毁界面
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            finish();
        });

        //账号
        user_name_et = findViewById(R.id.iv_user);
        //密码
        user_pass_et = findViewById(R.id.iv_pass);

        //登录
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            requestServer("user_logon", (EditText) user_name_et, (EditText) user_pass_et);
        });

        //注册账号
        TextView btn_reg = findViewById(R.id.btn_reg);
        btn_reg.setOnClickListener(v -> {
            requestServer("user_reg", (EditText) user_name_et, (EditText) user_pass_et);
        });

        //切换注册或登录

        TextView switch_account = findViewById(R.id.switch_account);
        switch_account.setOnClickListener(v -> {

            String account = switch_account.getText().toString();
            if (account.equals("注册账号")) {
                btn_login.setVisibility(View.GONE);
                btn_reg.setVisibility(View.VISIBLE);
                switch_account.setText("已有账号");
            }
            if (account.equals("已有账号")) {
                btn_login.setVisibility(View.VISIBLE);
                btn_reg.setVisibility(View.GONE);
                switch_account.setText("注册账号");
            }
        });




    }


    /**
     * 请求服务器@茶茶QQ205888578
     *
     * @param uNameET    用户名输入框
     * @param uPassET    密码输入框
     */

    private void requestServer(String act, EditText uNameET, EditText uPassET) {
        //获取数据
        final String userName = uNameET.getText().toString().trim();
        final String userPassWord = uPassET.getText().toString().trim();
        //非空判断
        if (TextUtils.isEmpty(userName)) {
            ToastUtils.showLong("您还没输入账号");
            return;
        }
        if (userName.length() < 5) {
            ToastUtils.showLong("输入的账号小于6位");
            return;
        }
        if (TextUtils.isEmpty(userPassWord)) {
            ToastUtils.showLong("您还没输入密码");
            return;
        }
        if (act.equals("user_reg")) {
            //正在注册
        } else {
            //正在登录
        }
        LoginRegs(act, userName, userPassWord);
    }


    private void LoginRegs(String act, String user, String passwd) { //注册登录
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi(act))
                    .params("user", user)
                    .params("account", user)
                    .params("password", passwd)
                    .params("markcode", ToolUtils.getAndroidId(LoginActivity.this))
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("user="+user+"&account="+user+"&password="+passwd+"&markcode="+ToolUtils.getAndroidId(this)))
                    .execute(new AbsCallback<String>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jo = new JSONObject(BaseR.decry_R(response.body()));

                                if (jo.getInt("code") == 200) { //成功
                                    if (act.equals("user_logon")) {
                                        MMkvUtils.saveUser(user);
                                        MMkvUtils.savePasswd(passwd);
                                        ToastUtils.showLong("登录成功");
                                        //((TextView)findViewById(R.id.user_fragment_Logout_text)).setText("退出登录");
                                        ReUserBean userData = new Gson().fromJson(BaseR.decry_R(response.body()), ReUserBean.class);


                                        MMkvUtils.saveReUserBean(userData);


                                        finish(); // 关闭当前界面

                                    } else {
                                        MMkvUtils.saveReUserBean(null);
                                        LoginRegs("user_logon", user, passwd);
                                    }
                                    //  findViewById(R.id.lv_user_Refresh).setVisibility(View.GONE);
                                } else {
                                    MMkvUtils.saveReUserBean(null);
                                    //其他提示
                                    ToastUtils.showLong(jo.getString("msg"));
                                }


                            } catch (JSONException e) {
                                MMkvUtils.saveReUserBean(null);
                                e.printStackTrace();
                            }

                        }


                        public void onError(Response<String> error) {
                            ToastUtils.showLong("未知错误");
                        }

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            assert response.body() != null;
                            return response.body().string();
                        }
                    });
        }).start();
    }



}