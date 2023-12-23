package com.github.tvbox.osc.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.base.BaseActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VipGoodsData;
import com.github.tvbox.osc.beanry.GoodsBean;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PayActivity extends AppCompatActivity implements VipGoodsAdapter.OnClickListener {

    private ArrayList<VipGoodsData> vipGoodsDataList = new ArrayList<>();
    private VipGoodsAdapter vipGoodsAdapter;
    private RecyclerView activity_vip_list;

    private String payOrder;
    private TextView tvMessage;


    @Override
    protected void onResume() {
        super.onResume();
        if (payOrder != null) {
            orderStatus(payOrder);
            payOrder = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        //沉浸状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        activity_vip_list = findViewById(R.id.activity_vip_list);
        //横向排列
        activity_vip_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //返回
        findViewById(R.id.ivBack).setOnClickListener(v -> {
            finish();
        });

        //卡密充值
        findViewById(R.id.tvMoney).setOnClickListener(v -> {
            exchangeCard();
        });

        tvMessage = findViewById(R.id.tvMessage);


       //tvMessage.setText("卡密充值：茶茶QQ205888578");


        //加载商品
        getVipListBean();
    }


    /**
     *  卡密充值
     *  茶茶QQ205888578
     */
    private void exchangeCard() {
        EditText etMoney = findViewById(R.id.etMoney);
        String key = etMoney.getText().toString();

        if (key.isEmpty()) {
            ToastUtils.showLong("输入的兑换码有误");
        } else {
            ReUserBean userBean = MMkvUtils.loadReUserBean("");
            if (userBean != null && ToolUtils.getIsEmpty(userBean.msg.token)) {
                recHarGe(key, userBean.msg.token);
            } else {
                ToastUtils.showLong("TOKEN过期！请重启应用");
            }
        }
    }

    private void recHarGe(String key, String token) {
        Log.d("token", "recHarGe: " + token);
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("card"))
                    .params("token", token)
                    .params("kami", key)
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("token=" + token + "&kami=" + key))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jo = new JSONObject(BaseR.decry_R(response.body()));
                                if (jo.getInt("code") == 200) {
                                    ToastUtils.showLong(jo.getString("msg"));
                                }else{
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

    private GoodsBean goodsData;

    /**
     * 获取商品列表
     * 茶茶QQ205888578
     */
    private void getVipListBean() {
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("goods"))
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("null"))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                           // findViewById(R.id.iv_anim_loading).setVisibility(View.GONE);
                            if (ToolUtils.iniData(response, PayActivity.this)) {
                                goodsData = new Gson().fromJson(BaseR.decry_R(response.body()), GoodsBean.class);
                                if (goodsData != null && goodsData.code == 200 && goodsData.msg.size() > 0) {
                                    //暂无
                                    //findViewById(R.id.iv_anim_null).setVisibility(View.GONE);
                                    for (int i = 0; i < goodsData.msg.size(); i++) {
                                        VipGoodsData vipGoodsData = new VipGoodsData();
                                        vipGoodsData.setDes(goodsData.msg.get(i).gname);
                                        vipGoodsData.setPrice(goodsData.msg.get(i).gmoney);
                                        vipGoodsData.setDesS(goodsData.msg.get(i).cv);
                                        vipGoodsDataList.add(vipGoodsData);
                                        vipGoodsAdapter = new VipGoodsAdapter(PayActivity.this, PayActivity.this, vipGoodsDataList);
                                        activity_vip_list.setAdapter(vipGoodsAdapter);
                                    }
                                }else{
                                   // findViewById(R.id.iv_anim_null).setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            assert response.body() != null;
                            return response.body().string();
                        }
                    });
        }).start();
    }


    private ReUserBean userBean;

    private String payType = "wx";

    @SuppressLint("SetTextI18n")
    @Override
    public void onGoodsItemClick(int position) {
        ReUserBean userBean = MMkvUtils.loadReUserBean("");
        if (goodsData != null && userBean != null) {
            int userId = userBean.msg.info.id;
            payOrder = getOrderNum(userId);
            String payUrl = ToolUtils.setApi("pay") + "&order=" + payOrder + "&token=" + userBean.msg.token + "&way=" + payType + "&gid=" + goodsData.msg.get(position).gid + "&ua=0" + "&t=";
            Intent newIntent = new Intent(this, WebViewActivity.class);
           // Log.d(TAG, "onGoodsItemClick: "+payUrl);
            newIntent.putExtra("newUrl", payUrl);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PayActivity.this.startActivity(newIntent);
        } else {
            Toast.makeText(this, "您未登录", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 生成订单号（25位）：时间（精确到毫秒）+3位随机数+5位用户id
     */
    public static synchronized String getOrderNum(int userId) {
       // Log.d(TAG, "getOrderNum: 111111111");
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String localDate = dateFormat.format(date);
        //3位随机数
        int i = (int) (Math.random() * 900 + 100);
        String randomNumeric = Integer.toString(i);
        String orderNum = localDate + randomNumeric + userId;
        Log.d("订单号:{}", orderNum);
        return orderNum;
    }


    /**
     * 查询订单状态
     */
    private void orderStatus(String out_trade_no) {
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("pay_res"))
                    .params("oid", out_trade_no)
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("oid=" + out_trade_no))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (ToolUtils.iniData(response, PayActivity.this)) {
                                ToastUtils.showLong("会员开通成功");
                            }
                           // getUserInfo(userBean.msg.token);
                        }

                        @Override
                        public void onError(Response<String> error) {
                            ToastUtils.showLong( BaseR.decry_R(error.toString()));
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