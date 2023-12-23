package com.github.tvbox.osc.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.beanry.InitBean;
import com.github.tvbox.osc.beanry.ReJieXiBean;
import com.github.tvbox.osc.beanry.ReUserBean;
import com.github.tvbox.osc.beanry.SiteBean;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.hawk.Hawk;
import com.shengqu.baquanapi.api.BaQuanAdSdk;
import com.shengqu.baquanapi.api.splash.BaQuanSplashAd;
import com.shengqu.baquanapi.api.splash.BaQuanSplashAdListener;
import com.shengqu.baquansdk.sdk.BaQuanAdError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import android.Manifest;

/**
 * @author 茶茶
 * @date :2023/12/19
 * @description:
 * 启动界面 茶茶QQ205888578
 */
public class QidongActivity extends AppCompatActivity {


    private String Mac;
    private final Handler handler = new Handler();
    private boolean isLogin = true;
    private ImageView imageView;
    private TextView countdownText;
    private LinearLayout countdownLayout;

    private int countdownTime = 5; // 倒计时时间，单位为秒
    private Runnable runnable;
    private BaQuanSplashAd mBaQuanSplashAd;
    private boolean mIsLaunch;
    private final int REQUEST_CODE = 1024;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qidong);

        imageView = findViewById(R.id.imageView);

        //后台对应的应用appId及应用appKey，填错不会计算收益！！！
        BaQuanAdSdk.init(this, "102374345411", "EYJ3jVwugeBmiFlpvcp2ohIpZcSGdlou");



        //沉浸状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //跳过
        countdownLayout = findViewById(R.id.countdownLayout);
        countdownLayout.setVisibility(View.GONE);
        countdownLayout.setOnClickListener(v -> {
             Intent intent = new Intent(QidongActivity.this, MainActivity.class);
             startActivity(intent);
            handler.removeCallbacks(runnable); // 停止倒计时
        });



        //判断是否动态域名
        if(HawkConfig.dt_ym.equals("0")) {
            getdtym();
        }
        else {
            getAppIni();
            getSite();
            getJieXi();
        }


        Intent intent = getIntent();
        mIsLaunch = intent.getBooleanExtra("is_launch", true);
        //版本判断，6.0以上进行动态权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            loadAd();
        }



    }

    /**
     * 请求权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        List<String> lackedPermission = new ArrayList<String>();
        if (!(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )){
            lackedPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!(checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // 如果需要的权限都已经有了，那么直接请求广告
        if (lackedPermission.size() == 0) {
            loadAd();
        } else {
            // 否则，建议请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, REQUEST_CODE);
        }
    }


    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(requestCode == REQUEST_CODE && hasAllPermissionsGranted(grantResults))) {
            Toast.makeText(this, "请同意相关权限", Toast.LENGTH_LONG);
        }
        //未同意权限也请求广告
        loadAd();
    }


    /**
     * 加载广告
     */
    private void loadAd() {

        //若启动就加载广告，建议延迟1s后创建对象
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                delayLoadAd();
            }
        }, 1000);
    }

    private void delayLoadAd() {

        mBaQuanSplashAd = new BaQuanSplashAd(this, "20960464201504", new BaQuanSplashAdListener() {
            @Override
            public void onAdLoadSuccess() {
                //广告加载成功
            }

            @Override
            public void onAdLoadFail(BaQuanAdError baQuanAdError) {
                //广告加载失败
                //goMainActivity();
            }

            @Override
            public void onAdRenderSuccess() {
                //广告渲染成功
            }

            @Override
            public void onAdRenderFail(BaQuanAdError baQuanAdError) {
                //广告渲染失败
              //  goMainActivity();
            }

            @Override
            public void onAdShow() {
                //广告展示成功
            }

            @Override
            public void onAdClick() {
                //广告被点击
            }

            @Override
            public void onAdClose() {
                //广告被关闭
              //  goMainActivity();
            }
        });

        ViewGroup adContainer = findViewById(R.id.fl_ad_container);

        //方式1：默认全屏展示
        mBaQuanSplashAd.loadAndShowAd(adContainer);

        //方式2：指定广告素材宽高，单位px，图片尺寸传入与展示区域大小设置需保持一致，避免素材变形；
      //  mBaQuanSplashAd.loadAndShowAd(adContainer, 1080, 1500);
    }

    /**
     * 跳转主页
     */
    private void goMainActivity() {
        //demo示例中判断是否为启动页，一般开发者无此需求
        if (mIsLaunch) {
            Intent intent = new Intent(QidongActivity.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //销毁广告
        if (mBaQuanSplashAd != null) {
            mBaQuanSplashAd.destroy();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    private void getdtym() {//获取动态域名
        OkGo.<String>get(HawkConfig.CONFIG_URL)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        // 获取响应内容
                        String content = response.body();

                        // 截取URL后面的字符
                        int index = content.indexOf("url=");
                        if (index != -1) {
                            String subString = content.substring(index + 4);
                            // System.out.println(subString);
                            HawkConfig.MMM_MMM = subString;
                            //ToastUtils.showShort(subString);
                            getAppIni();
                            getSite();
                            getJieXi();
                        } else {
                            //找不到使用本地
                            // ToastUtils.showShort("域名获取失败");
                            getAppIni();
                            getSite();
                            getJieXi();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        // 请求失败处理
                        //  ToastUtils.showShort("获取动态域名失败");
                        Throwable throwable = response.getException();
                        throwable.printStackTrace();
                        //无网
                        ToastUtils.showShort("请检查网络连接");
                        //显示跳过按钮
                        countdownLayout.setVisibility(View.VISIBLE);
                    }
                });

    }


    private void getAppIni() { //获取应用配置
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("ini") + "&pay")
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("pay"))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (ToolUtils.iniData(response, QidongActivity.this)) {
                                InitBean initData = new Gson().fromJson(BaseR.decry_R(response.body()), InitBean.class);
                                if (initData.code == 200) {
                                    String apiJson = initData.msg.appJson;
                                    if (!ToolUtils.getIsEmpty(apiJson) && ToolUtils.getIsEmpty(initData.msg.appJsonb)) {
                                        apiJson = initData.msg.appJsonb;
                                    }
                                    Hawk.put(HawkConfig.JSON_URL, apiJson); //保存聚合接口
                                    String apiJsonc = initData.msg.appJsonc;
                                    Hawk.put(HawkConfig.JSON_URL2, apiJsonc); //保存多线接口


                                    //显示跳过按钮
                                    countdownLayout.setVisibility(View.VISIBLE);
                                    //倒计时
                                    countdownText = findViewById(R.id.countdownText);
                                    startCountdown();

                                    //启动图
                                    String startupAd = initData.msg.uiStartad;
                                    if (ToolUtils.getIsEmpty(startupAd)) {
                                        setQiDong(startupAd);
                                    }

                                    MMkvUtils.saveInitBean(initData);

                              /*      if (ToolUtils.getIsEmpty(initData.msg.logonWay)) {
                                        switch (initData.msg.logonWay) {
                                            case "0":
                                                if (!MMkvUtils.loadUser().equals("") && !MMkvUtils.loadPasswd().equals("")) {
                                                   // reLoginReg(MMkvUtils.loadUser(), MMkvUtils.loadPasswd());
                                                }
                                                break;
                                            case "1":
                                               // Log.d(TAG, "卡密登录");
                                                break;
                                            case "2":
                                                if (!MMkvUtils.loadUser().equals("") && !MMkvUtils.loadPasswd().equals("")) {
                                                   // reLoginReg(MMkvUtils.loadUser(), MMkvUtils.loadPasswd());
                                                } else {
                                                  //  reLoginReg(Mac, "12345678");
                                                }
                                                break;
                                        }
                                    }*/


                                    // handler.postDelayed(runnable, 1000);
                                }else {
                                    ToastUtils.showShort("服务器连接失败");

                                    //显示跳过按钮
                                    countdownLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onError(final Response<String> error) {
                            //无网
                            ToastUtils.showShort("网络连接失败");
                            // 倒计时结束，跳转到其他界面
                            Intent intent = new Intent(QidongActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // 关闭当前界面
                        }

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            assert response.body() != null;
                            return response.body().string();
                        }
                    });
        }).start();
    }


    private void getSite() { //获取自定义站点
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("site"))
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("null"))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onStart(Request<String, ? extends Request> request) {
                            Log.d("QidongActivity", "onStart333: " + request.getCacheKey());
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            if (ToolUtils.iniData(response, QidongActivity.this)) {
                                SiteBean siteDta = new Gson().fromJson(BaseR.decry_R(response.body()), SiteBean.class);
                                MMkvUtils.saveSiteBean(siteDta);
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


    private void getJieXi() { //获取解析接口
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("exten"))
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("null"))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (ToolUtils.iniData(response, QidongActivity.this)) {
                                ReJieXiBean reJieXiBean = new Gson().fromJson(BaseR.decry_R(response.body()), ReJieXiBean.class);
                                MMkvUtils.saveReJieXiBean(reJieXiBean);
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


    private void startCountdown() {
        runnable = new Runnable() {
            @Override
            public void run() {
                countdownText.setText(String.valueOf(countdownTime));

                if (countdownTime > 0) {
                    countdownTime--;
                    handler.postDelayed(this, 1000); // 每隔1秒执行一次
                } else {
                    // 倒计时结束，跳转到其他界面
                    Intent intent = new Intent(QidongActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // 关闭当前界面
                }
            }
        };

        handler.post(runnable);
    }


    private void setQiDong(String imgUrl) {
        if (!isDestroyed()) {
        // imageView.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imgUrl)
                .centerCrop()
                .override(0, 0) //默认淡入淡出动画
                .transition(DrawableTransitionOptions.withCrossFade()) //缓存策略,跳过内存缓存【此处应该设置为false，否则列表刷新时会闪一下】
                .skipMemoryCache(false) //缓存策略,硬盘缓存-仅仅缓存最终的图像，即降低分辨率后的（或者是转换后的）
                .diskCacheStrategy(DiskCacheStrategy.ALL) //设置图片加载的优先级
                .priority(Priority.HIGH)
                .into(imageView);
        }
    }


}