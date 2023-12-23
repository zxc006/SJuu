package com.github.tvbox.osc.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.beanry.ReUserBean;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author 茶茶
 * @date :2023/12/19
 * @description:
 * 修改昵称 茶茶QQ205888578
 */

public class NichengActivity extends AppCompatActivity {

    private TextView nicheng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nicheng);

        //沉浸状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        nicheng = findViewById(R.id.nicheng);



        findViewById(R.id.niFinish).setOnClickListener(v -> {

            String textnicheng = nicheng.getText().toString();
            ReUserBean userBean = MMkvUtils.loadReUserBean("");

            if (textnicheng.isEmpty()) {
                ToastUtils.showLong("请输入昵称");
            }
            else {
                // 对昵称进行Base64编码
                String encodedNicheng = Base64.encodeToString(textnicheng.getBytes(), Base64.DEFAULT);
                recHarGe(userBean.msg.token, encodedNicheng);
            }
        });

        //返回
        findViewById(R.id.ni_rlBack).setOnClickListener(v -> {
            finish();
        });





    }


    private void recHarGe(String token, String name) {
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("alter_name"))
                    .params("token", token)
                    .params("name", name)
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("token="+token+"+name="+name))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jo = new JSONObject(BaseR.decry_R(response.body()));
                                if (jo.getInt("code") == 200) {
                                    //成功
                                    ToastUtils.showLong(jo.getString("msg"));


                                }
                                else {
                                    //失败
                                    ToastUtils.showLong(jo.getString("msg"));
                                }

                            } catch (JSONException e) {
                                ToastUtils.showLong(e.toString());
                                e.printStackTrace();
                            }

                        }


                        @Override
                        public void onError(Response<String> error) {
                            ToastUtils.showLong(BaseR.decry_R(error.body()));
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