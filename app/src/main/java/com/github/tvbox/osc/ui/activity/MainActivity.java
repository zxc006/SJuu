package com.github.tvbox.osc.ui.activity;

import static com.github.tvbox.osc.ui.activity.LiveActivity.context;
import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.base.BaseVbActivity;
import com.github.tvbox.osc.beanry.InitBean;
import com.github.tvbox.osc.databinding.ActivityMainBinding;
import com.github.tvbox.osc.ui.fragment.GridFragment;
import com.github.tvbox.osc.ui.fragment.HomeFragment;
import com.github.tvbox.osc.ui.fragment.MyFragment;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.github.tvbox.osc.view.HomeDialog;
import com.shengqu.baquanapi.api.interstitial.BaQuanInterstitialAd;
import com.shengqu.baquanapi.api.interstitial.BaQuanInterstitialAdListener;
import com.shengqu.baquansdk.sdk.BaQuanAdError;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseVbActivity<ActivityMainBinding> {

    List<Fragment> fragments = new ArrayList<>();

    public boolean useCacheConfig = false;

    private InitBean initData;
    private final Handler mHandler = new Handler();
    private long mExitTime = 0;
    private float countSize;    //软件更新总大小
    private float currentSize;    //软件更新当前下载进度
    private TextView tishi;
    private BaQuanInterstitialAd mBaQuanInterstitialAd;


    @Override
    protected void init() {
        useCacheConfig = false;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            useCacheConfig = bundle.getBoolean("useCache", false);
        }

        initVp();
        mBinding.bottomNav.setOnNavigationItemSelectedListener(menuItem -> {
            mBinding.vp.setCurrentItem(menuItem.getOrder(), false);
            return true;
        });
        mBinding.vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBinding.bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });

        initData= MMkvUtils.loadInitBean("");
        getAppVersion();

        //广告233233233233233233（@茶茶QQ205888578）
        loadAd();
    }

    private void initVp() {
        fragments.add(new HomeFragment());
        fragments.add(new MyFragment());
        mBinding.vp.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });
        mBinding.vp.setOffscreenPageLimit(fragments.size());
    }

    private long exitTime = 0L;

    @Override
    public void onBackPressed() {
        if (mBinding.vp.getCurrentItem() == 1) {
            mBinding.vp.setCurrentItem(0);
            return;
        }
        HomeFragment homeFragment = (HomeFragment) fragments.get(0);
        if (!homeFragment.isAdded()) {// 资源不足销毁重建时未挂载到activity时getChildFragmentManager会崩溃
            confirmExit();
            return;
        }
        List<BaseLazyFragment> childFragments = homeFragment.getAllFragments();
        if (childFragments.isEmpty()) {//加载中(没有tab)
            confirmExit();
            return;
        }
        Fragment fragment = childFragments.get(homeFragment.getTabIndex());
        if (fragment instanceof GridFragment) {// 首页数据源动态加载的tab
            GridFragment item = (GridFragment) fragment;
            if (!item.restoreView()) {// 有回退的view,先回退(AList等文件夹列表),没有可回退的,返到主页tab
                if (!homeFragment.scrollToFirstTab()){
                    confirmExit();
                }
            }
        } else {
            confirmExit();
        }
    }

    private void confirmExit() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtils.showShort("再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            ActivityUtils.finishAllActivities(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    private void getAppVersion() {
        if (initData != null && ToolUtils.getIsEmpty(initData.msg.appBb)) {
            String dVersion = initData.msg.appBb; //最新版本
            String bVersion = ToolUtils.getVersion(mContext); //本地版本
            bVersion = bVersion.toLowerCase();
            dVersion = dVersion.toLowerCase();
            if (dVersion.compareTo(bVersion) > 0) {
                //有更新进行升级
                showUpdateDialog(initData.msg.appNshow, initData.msg.appNurl, 1);
            } else {
                isNotice = true;
            }
        }
    }

    private boolean isNotice = false;

    private String newApkUrl;

    /**
     * 显示升级提示的对话框
     */
    private void showUpdateDialog(String text, final String apkUrl, int isRequired) {
        HomeDialog.Builder builder = new HomeDialog.Builder(mContext);
        builder.setTitle("发现新版本");
        String[] remarks = text.split(";");
        String str = "";
        for (int i = 0; i < remarks.length; i++) {
            if (i == remarks.length - 1) {
                str = str + remarks[i];
            } else {
                str = str + remarks[i] + "\n";
            }
        }
        builder.setMessage(str);
        if (isRequired == 1) {
            builder.setPositiveButton("等不及了，立即更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                    startActivity(intent);
                }
            });
        } else {
            builder.setPositiveButton("等不及了，立即更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                    startActivity(intent);
                }
            });
            builder.setNeutralButton("先看片呢，稍后提醒", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   // Screensaver.removeMessages(1);
                   // Screensaver.sendEmptyMessageDelayed(1, 10000);
                    dialog.dismiss();
                }
            });
        }
        builder.create().show();
    }


    private void loadAd() {
        mBaQuanInterstitialAd = new BaQuanInterstitialAd(this, "20643464201666", new BaQuanInterstitialAdListener() {
            @Override
            public void onAdLoadSuccess() {
                //广告加载成功
            }

            @Override
            public void onAdLoadFail(BaQuanAdError baQuanAdError) {
                //广告加载失败
               // ToastUtils.showShort("广告加载失败");
            }

            @Override
            public void onAdShow() {
                //广告展示
            }

            @Override
            public void onAdClick() {
                //广告被点击
            }

            @Override
            public void onAdClose() {
                //广告被关闭
            }

            @Override
            public void onAdVideoComplete() {
                //视频播放完毕
            }

            @Override
            public void onAdSkippedVideo() {
                //视频广告跳过
            }
        });

        mBaQuanInterstitialAd.loadAndShowAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //销毁广告
        if (mBaQuanInterstitialAd != null) {
            mBaQuanInterstitialAd.destroy();
        }
    }

}