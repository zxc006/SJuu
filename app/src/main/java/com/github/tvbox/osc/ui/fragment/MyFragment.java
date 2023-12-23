package com.github.tvbox.osc.ui.fragment;

import static com.github.tvbox.osc.player.thirdparty.Kodi.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseVbFragment;
import com.github.tvbox.osc.beanry.InitBean;
import com.github.tvbox.osc.beanry.ReUserBean;
import com.github.tvbox.osc.beanry.UserInfoBean;
import com.github.tvbox.osc.databinding.FragmentMyBinding;
import com.github.tvbox.osc.ui.activity.CollectActivity;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.DxianluActivity;
import com.github.tvbox.osc.ui.activity.ExchangeActivity;
import com.github.tvbox.osc.ui.activity.HistoryActivity;
import com.github.tvbox.osc.ui.activity.LiveActivity;
import com.github.tvbox.osc.ui.activity.LoginActivity;
import com.github.tvbox.osc.ui.activity.MovieFoldersActivity;
import com.github.tvbox.osc.ui.activity.PayActivity;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.activity.SubscriptionActivity;
import com.github.tvbox.osc.ui.activity.ZiliaoActivity;
import com.github.tvbox.osc.ui.dialog.AboutDialog;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.github.tvbox.osc.util.Utils;
//import com.github.tvbox.osc.util.WiFiDialog;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lxj.xpopup.XPopup;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * @author pj567
 * @date :2021/3/9
 * @description:
 * 我的界面 茶茶QQ205888578
 */
public class MyFragment extends BaseVbFragment<FragmentMyBinding> {

    private ImageView lvUserRefresh;
    private TextView tvUserMac;
    private TextView tvUserPoints;
    private TextView tvUserEndTime;
    private TextView myuserid;
    private InitBean initBean;


    //登录之后进行刷新界面数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
         initData();
    }

    @Override
    protected void init() {
        mBinding.tvVersion.setText("v"+ AppUtils.getAppVersionName());

        //播放链接
        mBinding.addrPlay.setOnClickListener(v ->{
            new XPopup.Builder(getContext())
                    .asInputConfirm("播放", "", isPush(ClipboardUtils.getText().toString())?ClipboardUtils.getText():"", "地址", text -> {
                        if (!TextUtils.isEmpty(text)){
                            Intent newIntent = new Intent(mContext, DetailActivity.class);
                            newIntent.putExtra("id", text);
                            newIntent.putExtra("sourceKey", "push_agent");
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(newIntent);
                        }
                    }, null, R.layout.dialog_input).show();
        });
        //mBinding.tvLive.setOnClickListener(v -> jumpActivity(LivePlayActivity.class));
        mBinding.tvLive.setOnClickListener(v -> jumpActivity(LiveActivity.class));

        mBinding.tvSetting.setOnClickListener(v -> jumpActivity(SettingActivity.class));

        //历史
        //mBinding.tvHistory.setOnClickListener(v -> jumpActivity(HistoryActivity.class));
        //收藏
        //mBinding.tvFavorite.setOnClickListener(v -> jumpActivity(CollectActivity.class));

        mBinding.tvLocal.setOnClickListener(v -> {
            if (!XXPermissions.isGranted(mContext, Permission.MANAGE_EXTERNAL_STORAGE)) {
                showPermissionTipPopup();
            } else {
                jumpActivity(MovieFoldersActivity.class);
            }
        });

        mBinding.llSubscription.setOnClickListener(v -> jumpActivity(SubscriptionActivity.class));

        //关于
        mBinding.llAbout.setOnClickListener(v -> {
            new XPopup.Builder(mActivity)
                    .asCustom(new AboutDialog(mActivity))
                    .show();
        });

        //用户昵称
        tvUserMac = mBinding.llUserMac;
        //用户id
        myuserid = mBinding.myId;
        //积分
        tvUserPoints = mBinding.llUserPrice;
        //会员
        tvUserEndTime = mBinding.llUserEndTime;

        //头像
        lvUserRefresh = mBinding.userActivityPic;

        //登录修改资料
        mBinding.myTiaozhuan.setOnClickListener(v -> {
            //检测是否登录
            ReUserBean userData = MMkvUtils.loadReUserBean("");
            if (userData != null) {
                // 已登录,跳转资料
                Intent intent = new Intent(App.getInstance(), ZiliaoActivity.class);
                startActivity(intent);
            } else {
                //未登录，跳转登录
                Intent intent = new Intent(App.getInstance(), LoginActivity.class);
                startActivity(intent);
            }

        });

        //登录修改资料
        mBinding.userActivityPic.setOnClickListener(v -> {
            //检测是否登录
            ReUserBean userData = MMkvUtils.loadReUserBean("");
            if (userData != null) {
                // 已登录,跳转资料
                Intent intent = new Intent(App.getInstance(), ZiliaoActivity.class);
                startActivity(intent);
            } else {
                //未登录，跳转登录
                Intent intent = new Intent(App.getInstance(), LoginActivity.class);
                startActivity(intent);
            }

        });


        //签到
        mBinding.myQiandao.setOnClickListener(v -> {
            exchangeCard();
        });

        //开通会员
        mBinding.myKthy.setOnClickListener(v -> {

            FastClickCheckUtil.check(v);
            if (initData != null) {
                if (initData.msg.pay.state.equals("y")) {
                    //在线支付
                    Intent intent = new Intent(App.getInstance(), PayActivity.class);
                    startActivity(intent);
                   // ToastUtils.showLong("是");
                } else {
                    //卡密兑换
                    Intent intent = new Intent(App.getInstance(), PayActivity.class);
                    startActivity(intent);
                   // ToastUtils.showLong("不是");
                }
            } else {
                ToastUtils.showLong("暂未开放");
            }

        });

        //选择线路
        mBinding.myXianlu.setOnClickListener(v -> {
            startActivity(new Intent(App.getInstance(), DxianluActivity.class));
        });
        //积分商城
        mBinding.myJifensc.setOnClickListener(v -> {
            startActivity(new Intent(App.getInstance(), ExchangeActivity.class));
        });
        //我的收藏
        mBinding.myShoucang.setOnClickListener(v -> {
            startActivity(new Intent(App.getInstance(), CollectActivity.class));
        });
        //历史记录
        mBinding.myLishi.setOnClickListener(v -> {
            startActivity(new Intent(App.getInstance(), HistoryActivity.class));
        });


        initBean = MMkvUtils.loadInitBean("");
        //客服
        mBinding.myKefu.setOnClickListener(v -> {
            if (initBean.msg.uiKefu.equals("")) {
                ToastUtils.showLong("获取QQ失败");
            } else {
                try {
                    String qqNumber = initBean.msg.uiKefu; // 获取QQ号码
                    String qqurl = "mqqwpa://im/chat?chat_type=wpa&uin=" + qqNumber;
                    // 跳转到联系QQ页面
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qqurl)));
                } catch (Exception e) {
                    String qurl = "客服QQ"+initBean.msg.uiKefu;
                    ToastUtils.showLong(qurl);
                    //ToastUtils.showLong("跳转QQ失败");
                }
            }
        });

        //Q群
        mBinding.myQun.setOnClickListener(v -> {

            if (initBean.msg.uiGroup.equals("")){
                ToastUtils.showLong("获取QQ群失败");
            }
            else {

                String qqGroupUrl = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=" + initBean.msg.uiGroup + "&card_type=group&source=qrcode";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qqGroupUrl)));
                } catch (Exception e) {
                    String qqurl2 = "官方Q群"+initBean.msg.uiGroup;
                    ToastUtils.showLong(qqurl2);
                   // ToastUtils.showLong("请安装QQ客端");
                }

            }
        });



        initData();
        vipCard();//判断是否vip显示图标
    }



    private void showPermissionTipPopup(){
        new XPopup.Builder(mActivity)
                .isDarkTheme(Utils.isDarkTheme())
                .asConfirm("提示","为了播放视频、音频等,我们需要访问您设备文件的读写权限", () -> {
                    getPermission();
                }).show();
    }

    private void getPermission(){
        XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            jumpActivity(MovieFoldersActivity.class);
                        }else {
                            ToastUtils.showLong("部分权限未正常授予,请授权");
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            ToastUtils.showLong("读写文件权限被永久拒绝，请手动授权");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(mActivity, permissions);
                        } else {
                            ToastUtils.showShort("获取权限失败");
                            showPermissionTipPopup();
                        }
                    }
                });
    }

    private boolean isPush(String text) {
        return !TextUtils.isEmpty(text) && Arrays.asList("smb", "http", "https", "thunder", "magnet", "ed2k", "mitv", "jianpian").contains(Uri.parse(text).getScheme());
    }

    private InitBean initData;
    private ReUserBean userData;

    @SuppressLint("SetTextI18n")
    private void initData() {
        initData = MMkvUtils.loadInitBean("");
        if (initData == null) {

        }


        userData = MMkvUtils.loadReUserBean("");
        if (userData != null && ToolUtils.getIsEmpty(userData.msg.token)) {

            //用户资料
            if (ToolUtils.getIsEmpty(MMkvUtils.loadUser())) {
                myuserid.setText("账号：" + MMkvUtils.loadUser());
            }else{
                myuserid.setText("用户：" + userData.msg.info.name);
            }
            //昵称
           tvUserMac.setText(userData.msg.info.name);

            tvUserPoints.setText("积分：" + userData.msg.info.fen);
            tvUserEndTime.setText("SVIP：" + ToolUtils.stampToDate(userData.msg.info.vip));
            getUserInfo(userData.msg.token, false);


            Glide.with(this)
                    .load(userData.msg.info.pic)
                    .error(R.drawable.app_icon)
                    .into(lvUserRefresh);
        } else {
            //未登录设置
            tvUserMac.setText("注册/登录");
            tvUserPoints.setText("积分：0");
            tvUserEndTime.setText("SVIP：未登录");
            myuserid.setText("账号：未登录");
           /* lluserRefreshtext.setText("立即登录");*/
        }
    }

    @SuppressLint("SetTextI18n")
    private void getUserInfo(String token, boolean i) {
        OkGo.<String>post(ToolUtils.setApi("get_info"))
                .params("token", token)
                .params("t", System.currentTimeMillis() / 1000)
                .params("sign", ToolUtils.setSign("token="+token))
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                                try {
                                    JSONObject jo = new JSONObject(BaseR.decry_R(response.body()));
                                    if (jo.getInt("code") == 200){
                                        UserInfoBean userInfoData = new Gson().fromJson(BaseR.decry_R(response.body()), UserInfoBean.class);
                                        userData.msg.info.vip = userInfoData.msg.vip;
                                        userData.msg.info.fen = userInfoData.msg.fen;
                                        userData.msg.info.name = userInfoData.msg.name;
                                        if (ToolUtils.getIsEmpty(MMkvUtils.loadUser())) {
                                            myuserid.setText("账号：" + MMkvUtils.loadUser());
                                        }else{
                                            myuserid.setText("用户：" + userData.msg.info.name);
                                        }
                                        //昵称
                                        tvUserMac.setText(userData.msg.info.name);
                                        tvUserPoints.setText("积分：" + userInfoData.msg.fen);
                                        tvUserEndTime.setText("SVIP：" + ToolUtils.stampToDate(userInfoData.msg.vip));
                                        MMkvUtils.saveReUserBean(userData);

                                        Log.e("TAG", "run:哈哈哈 " + userData.msg.info.pic);

                                    }else{
                                        if (i){
                                            ToastUtils.showLong("您的账号在其他设备登录！您已被迫下线");
                                        }
                                        //清空登录数据
                                        MMkvUtils.saveReUserBean(null);
                                    }
                                } catch (JSONException e) {
                                    if (i){
                                        ToastUtils.showLong("刷新失败");
                                    }
                                    MMkvUtils.saveReUserBean(null);
                                    e.printStackTrace();
                                }

                    }

                    @Override
                    public void onError(Response<String> error) {
                        ToastUtils.showLong("未知错误");
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        assert response.body() != null;
                        return response.body().string();
                    }
                });
    }


    /**
     *签到
     */

    private void exchangeCard() {


        //检测是否登录
        ReUserBean userBean = MMkvUtils.loadReUserBean("");
        if (userData != null) {
            //已登录
            if (userBean != null && ToolUtils.getIsEmpty(userBean.msg.token)) {
                recHarGe(userBean.msg.token);
            } else {
                ToastUtils.showLong("TOKEN过期！请重启应用");
            }

        } else {
            //未登录，跳转登录
            Intent intent = new Intent(App.getInstance(), LoginActivity.class);
            startActivity(intent);
        }

    }


    private void recHarGe(String token) {
        Log.d("token", "recHarGe: " + token);
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("clock"))
                    .params("token", token)
                    .params("t", System.currentTimeMillis() / 1000)
                    .params("sign", ToolUtils.setSign("token="+token))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jo = new JSONObject(BaseR.decry_R(response.body()));
                                if (jo.getInt("code") == 200) {
                                //签到成功
                                    ToastUtils.showLong(jo.getString("msg"));
                                    //重新加载数据
                                    initData();
                                    //刷新
                                    if (userData != null && ToolUtils.getIsEmpty(userData.msg.token)) {
                                        getUserInfo(userData.msg.token, false);
                                    }

                                }
                                else {
                                    //签到失败
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


    //判断是否会员
    private void vipCard() {
        ReUserBean userBean = MMkvUtils.loadReUserBean("");

      //  TextView logoutText = findViewById(R.id.user_fragment_Logout_text);
        if (userData != null) {
            //已登录
            if (userBean != null && ToolUtils.getIsEmpty(userBean.msg.token)) {
                shifouvip(userBean.msg.token);
            } else {
                //token过期
                ToastUtils.showLong("TOKEN过期！请重启应用");
            }

        } else {
            // 未登录
            mBinding.myVip.setVisibility(View.GONE);
        }
    }


    private void shifouvip(String token) {
        Log.d("token", "recHarGe: " + token);
        new Thread(() -> {
            OkGo.<String>post(ToolUtils.setApi("get_vip"))
                    .params("token", token)
                    .params("t", "")  //
                    .params("sign", ToolUtils.setSign("token=" + token  ))
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jo = new JSONObject(BaseR.decry_R(response.body()));
                                if (jo.getInt("code") == 200) {
                                    //是vip
                                    mBinding.myVip.setVisibility(View.VISIBLE);
                                    mBinding.myVip.setImageResource(R.drawable.user_h2vip);
                                }
                                else {
                                    //不是vip
                                    mBinding.myVip.setVisibility(View.GONE);
                                }

                            } catch (JSONException e) {
                                ToolUtils.showToast(mContext, e.toString(), R.drawable.toast_err);
                                e.printStackTrace();
                            }

                        }


                        @Override
                        public void onError(Response<String> error) {
                            ToolUtils.showToast(mContext, BaseR.decry_R(error.body()), R.drawable.toast_err);
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