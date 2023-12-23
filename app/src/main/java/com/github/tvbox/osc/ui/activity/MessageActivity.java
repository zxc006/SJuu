package com.github.tvbox.osc.ui.activity;

import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.beanry.NoticeAdapter;
import com.github.tvbox.osc.beanry.NoticeBean;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.ToolUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;


/**
 * @茶茶QQ205888578
 * @date :2023/9/25
 * 我的消息界面
 */
public class MessageActivity extends BaseActivity {


    // private AlwaysMarqueeTextView gongGao;

    private NoticeAdapter noticeAdapter;
    private RecyclerView message_list;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_message;
    }

    @Override
    protected void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(layoutParams);
        }

        // gongGao = findViewById(R.id.wogonggao);
        message_list = findViewById(R.id.message_list);

        //返回
        findViewById(R.id.rlBack).setOnClickListener(v -> {
            finish();
        });

        getNotice();

    }



    private boolean isNotice = false;
    //公告
    private void getNotice() {
        Log.d("tang","getNotice");
        OkGo.<String>post(ToolUtils.setApi("notice"))
                .params("t", System.currentTimeMillis() / 1000)
                .params("sign", ToolUtils.setSign("null"))
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (ToolUtils.iniData(response, mContext)) {
                            NoticeBean noticeData = new Gson().fromJson(BaseR.decry_R(response.body()), NoticeBean.class);
                            if (noticeData != null && noticeData.msg.size() > 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        noticeAdapter = new NoticeAdapter(noticeData.msg);
                                        message_list.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                                        message_list.setAdapter(noticeAdapter);
                                    }
                                });
                             /*   if (ToolUtils.getIsEmpty(noticeData.msg.get(noticeData.msg.size() - 1).content)) {
                                    ToastUtils.showShort("暂无公告");


                                  *//*  if (isNotice)
                                        ToolUtils.HomeDialog(mContext, noticeData.msg.get(noticeData.msg.size() - 1).content);*//*
                                }*/
                            }else {
                                ToastUtils.showShort("暂无公告");
                            }
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        assert response.body() != null;
                        return response.body().string();
                    }
                });
    }



}