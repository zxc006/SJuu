package com.github.tvbox.osc.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.beanry.InitBean;
import com.github.tvbox.osc.beanry.ReUserBean;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;

/**
 * @author 茶茶
 * @date :2023/12/19
 * @description:
 * 资料界面 茶茶QQ205888578
 */

public class ZiliaoActivity extends AppCompatActivity {

    private ReUserBean userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ziliao);

        //沉浸状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        userData = MMkvUtils.loadReUserBean("");

        //返回
        findViewById(R.id.zl_rlBack).setOnClickListener(v -> {
            finish();
        });

   /*     //更换头像
        findViewById(R.id.tvChangeAvator).setOnClickListener(v -> {
            ToastUtils.showLong("没写，你换个球球,茶茶QQ205888578，下次在写");
        });*/

        //更换昵称
        findViewById(R.id.tvChangeNickname).setOnClickListener(v -> {
            startActivity(new Intent(ZiliaoActivity.this, NichengActivity.class));
        });

        //更换密码
        findViewById(R.id.findpass).setOnClickListener(v -> {
            startActivity(new Intent(ZiliaoActivity.this, PassActivity.class));
        });


        //退出
        findViewById(R.id.tvLogout).setOnClickListener(v -> {
            if (userData != null && ToolUtils.getIsEmpty(userData.msg.token)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("确认退出");
                builder.setMessage("确定要退出登录吗？");
                builder.setPositiveButton("确定", (dialog, which) -> {
                    MMkvUtils.saveUser(null);
                    MMkvUtils.savePasswd(null);
                    MMkvUtils.saveReUserBean(null);

                    //退出
                    finish();
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            }else {
                ToastUtils.showLong("你都没登录你退出个毛线");
            }

        });



    }
}