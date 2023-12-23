package com.github.tvbox.osc.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.angcyo.tablayout.delegate.ViewPager1Delegate;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.base.BaseVbFragment;
import com.github.tvbox.osc.bean.AbsSortXml;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.beanry.AdvBean;
import com.github.tvbox.osc.beanry.InitBean;
import com.github.tvbox.osc.databinding.FragmentHomeBinding;
import com.github.tvbox.osc.server.ControlManager;

import com.github.tvbox.osc.ui.activity.CollectActivity;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.DxianluActivity;
import com.github.tvbox.osc.ui.activity.FastSearchActivity;
import com.github.tvbox.osc.ui.activity.HistoryActivity;
import com.github.tvbox.osc.ui.activity.LiveActivity;
import com.github.tvbox.osc.ui.activity.MainActivity;
import com.github.tvbox.osc.ui.activity.MessageActivity;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.activity.SubscriptionActivity;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.TipDialog;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.stx.xhb.androidx.XBanner;
import com.stx.xhb.androidx.transformers.Transformer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends BaseVbFragment<FragmentHomeBinding> {
    private SourceViewModel sourceViewModel;
    private List<BaseLazyFragment> fragments = new ArrayList<>();
    private Handler mHandler = new Handler();

    /**
     * 顶部tabs分类集合,用于渲染tab页,每个tab对应fragment内的数据
     */
    private List<MovieSort.SortData> mSortDataList = new ArrayList<>();
    private boolean dataInitOk = false;
    private boolean jarInitOk = false;


    @Override
    protected void init() {
        ControlManager.get().startServer();

        mBinding.nameContainer.setOnClickListener(v -> {
            if (dataInitOk && jarInitOk) {
                showSiteSwitch();
            } else {
                ToastUtils.showShort("数据源未加载，长按刷新或切换订阅");
            }
        });

        mBinding.nameContainer.setOnLongClickListener(v -> {
            refreshHomeSouces();
            return true;
        });

        mBinding.search.setOnClickListener(view -> jumpActivity(FastSearchActivity.class));
        mBinding.ivHistory.setOnClickListener(view -> jumpActivity(HistoryActivity.class));
        mBinding.ivCollect.setOnClickListener(view -> jumpActivity(CollectActivity.class));
        setLoadSir(mBinding.contentLayout);

        //多线路
        mBinding.hoXianlu.setOnClickListener(v -> {
            startActivity(new Intent(mContext, DxianluActivity.class));
        });
        //电视直播
        mBinding.hoZhibo.setOnClickListener(v -> {
            startActivity(new Intent(mContext, LiveActivity.class));
        });
        //播放链接
        mBinding.hoLianjie.setOnClickListener(v -> {
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
        //公告
        mBinding.hoGonggao.setOnClickListener(v -> {
            startActivity(new Intent(mContext, MessageActivity.class));
        });



        initViewModel();
        initData();
        getAdv();

        //Java版
        mBinding.xbanner.loadImage(new XBanner.XBannerAdapter() {
            @Override
            public void loadBanner(XBanner banner, Object model, View view, int position) {
                AdvBean.MsgDTO bean = ((AdvBean.MsgDTO)model);
                Glide.with(getActivity())
                        .load(bean.getXBannerUrl())
                        .override(1280, 720) // 设置图片大小像素
                        .transform(new RoundedCorners(30))//设置图片圆角
                        .into((ImageView) view);
            }
        });


    }


    private void initViewModel() {
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
        sourceViewModel.sortResult.observe(this, absXml -> {
            showSuccess();
            if (absXml != null && absXml.classes != null && absXml.classes.sortList != null) {
                mSortDataList = DefaultConfig.adjustSort(ApiConfig.get().getHomeSourceBean().getKey(), absXml.classes.sortList, true);
            } else {
                mSortDataList = DefaultConfig.adjustSort(ApiConfig.get().getHomeSourceBean().getKey(), new ArrayList<>(), true);
            }
            initViewPager(absXml);
        });
    }

    private void initData() {

        MainActivity mainActivity = (MainActivity) mActivity;

        SourceBean home = ApiConfig.get().getHomeSourceBean();
        if (home != null && home.getName() != null && !home.getName().isEmpty()) {
            mBinding.tvName.setText(home.getName());
            mBinding.tvName.postDelayed(() -> mBinding.tvName.setSelected(true), 2000);
        }
        if (dataInitOk && jarInitOk) {
            showLoading();
            sourceViewModel.getSort(ApiConfig.get().getHomeSourceBean().getKey());
            return;
        }
        showLoading();
        if (dataInitOk && !jarInitOk) {
            if (!ApiConfig.get().getSpider().isEmpty()) {
                ApiConfig.get().loadJar(mainActivity.useCacheConfig, ApiConfig.get().getSpider(), new ApiConfig.LoadConfigCallback() {
                    @Override
                    public void success() {
                        jarInitOk = true;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!mainActivity.useCacheConfig)
                                    ToastUtils.showShort("更新订阅成功");
                                initData();
                            }
                        }, 50);
                    }

                    @Override
                    public void retry() {

                    }

                    @Override
                    public void error(String msg) {
                        jarInitOk = true;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("更新订阅失败");
                                initData();
                            }
                        });
                    }
                });
            }
            return;
        }
        ApiConfig.get().loadConfig(mainActivity.useCacheConfig, new ApiConfig.LoadConfigCallback() {
            TipDialog dialog = null;

            @Override
            public void retry() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                });
            }

            @Override
            public void success() {
                dataInitOk = true;
                if (ApiConfig.get().getSpider().isEmpty()) {
                    jarInitOk = true;
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                }, 50);
            }

            @Override
            public void error(String msg) {
                if (msg.equalsIgnoreCase("-1")) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataInitOk = true;
                            jarInitOk = true;
                            initData();
                        }
                    });
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog == null)
                            dialog = new TipDialog(getActivity(), msg, "重试", "取消", new TipDialog.OnListener() {
                                @Override
                                public void left() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            initData();
                                            dialog.hide();
                                        }
                                    });
                                }

                                @Override
                                public void right() {
                                    dataInitOk = true;
                                    jarInitOk = true;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            initData();
                                            dialog.hide();
                                        }
                                    });
                                }

                                @Override
                                public void cancel() {
                                    dataInitOk = true;
                                    jarInitOk = true;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            initData();
                                            dialog.hide();
                                        }
                                    });
                                }

                                @Override
                                public void onTitleClick() {
                                    dialog.hide();
                                    jumpActivity(SubscriptionActivity.class);
                                }
                            });
                        if (!dialog.isShowing())
                            dialog.show();
                    }
                });
            }
        }, getActivity());
    }

    private TextView getTabTextView(String text) {
        TextView textView = new TextView(mContext);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(ConvertUtils.dp2px(20), ConvertUtils.dp2px(10), ConvertUtils.dp2px(5), ConvertUtils.dp2px(10));
        return textView;
    }

    private void initViewPager(AbsSortXml absXml) {
        if (!mSortDataList.isEmpty()) {
            mBinding.tabLayout.removeAllViews();
            fragments.clear();
            for (MovieSort.SortData data : mSortDataList) {
                mBinding.tabLayout.addView(getTabTextView(data.name));

                if (data.id.equals("my0")) {//tab是主页,添加主页fragment 根据设置项显示豆瓣热门/站点推荐(每个源不一样)/历史记录
                    if (Hawk.get(HawkConfig.HOME_REC, 0) == 1 && absXml != null && absXml.videoList != null && absXml.videoList.size() > 0) {//站点推荐
                        fragments.add(UserFragment.newInstance(absXml.videoList));
                    } else {//豆瓣热门/历史记录
                        fragments.add(UserFragment.newInstance(null));
                    }
                } else {//来自源的分类
                    fragments.add(GridFragment.newInstance(data));
                }
            }

            if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {//关闭主页
                mBinding.tabLayout.removeViewAt(0);
                fragments.remove(0);
            }

            //重新渲染vp
            mBinding.mViewPager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager()) {
                @NonNull
                @Override
                public Fragment getItem(int position) {
                    return fragments.get(position);
                }

                @Override
                public int getCount() {
                    //主页才显示轮播图
                    llbbTab();
                    return fragments.size();
                }


            });
            //tab和vp绑定
            ViewPager1Delegate.Companion.install(mBinding.mViewPager, mBinding.tabLayout, true);
        }
    }

    /**
     * 提供给主页返回操作
     */
    public boolean scrollToFirstTab() {
        if (mBinding.tabLayout.getCurrentItemIndex() != 0) {
            mBinding.mViewPager.setCurrentItem(0, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 只有首页显示轮播图和图标
     */
    public boolean llbbTab() {
        if (mBinding.tabLayout.getCurrentItemIndex() != 0) {
            mBinding.homeLblb.setVisibility(View.GONE);
            return true;
        } else {
            mBinding.homeLblb.setVisibility(View.VISIBLE);
            return false;
        }
    }

    /**
     * 提供给主页返回操作
     */
    public int getTabIndex() {
        return mBinding.tabLayout.getCurrentItemIndex();
    }

    /**
     * 提供给主页返回操作
     */
    public List<BaseLazyFragment> getAllFragments() {
        return fragments;
    }


    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    void showSiteSwitch() {
        List<SourceBean> sites = ApiConfig.get().getSourceBeanList();
        if (sites.size() > 0) {
            SelectDialog<SourceBean> dialog = new SelectDialog<>(getActivity());
            TvRecyclerView tvRecyclerView = dialog.findViewById(R.id.list);

            tvRecyclerView.setLayoutManager(new V7GridLayoutManager(dialog.getContext(), 2));

            dialog.setTip("请选择首页数据源");
            dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<SourceBean>() {
                @Override
                public void click(SourceBean value, int pos) {
                    ApiConfig.get().setSourceBean(value);
                    refreshHomeSouces();
                }

                @Override
                public String getDisplay(SourceBean val) {
                    return val.getName();
                }
            }, new DiffUtil.ItemCallback<SourceBean>() {
                @Override
                public boolean areItemsTheSame(@NonNull @NotNull SourceBean oldItem, @NonNull @NotNull SourceBean newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull @NotNull SourceBean oldItem, @NonNull @NotNull SourceBean newItem) {
                    return oldItem.getKey().equals(newItem.getKey());
                }
            }, sites, sites.indexOf(ApiConfig.get().getHomeSourceBean()));
            dialog.show();
        } else {
            ToastUtils.showLong("暂无可用数据源");
        }
    }

    private void refreshHomeSouces() {
        Intent intent = new Intent(App.getInstance(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean("useCache", true);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ControlManager.get().stopServer();
    }


    /**
     * 调用接口获取轮播图
     */
    private void getAdv() {
        Log.d("tang", "getNotice");
        OkGo.<String>post(ToolUtils.setApi("homead"))
                .params("t", System.currentTimeMillis() / 1000)
                .params("sign", ToolUtils.setSign("null"))
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        if (ToolUtils.iniData(response, mContext)) {
                            AdvBean noticeData = new Gson().fromJson(BaseR.decry_R(response.body()), AdvBean.class);

                            Log.d("bannerData1", new Gson().toJson(noticeData));
                            if (noticeData != null && noticeData.msg.size() > 0) {
                                List<AdvBean.MsgDTO> bannerData = noticeData.msg;


                                //刷新数据之后，需要重新设置是否支持自动轮播
                                mBinding.xbanner.setAutoPlayAble(bannerData.size() > 1);
                                //设置模式是否为一屏多页（可选）
                                mBinding.xbanner.setIsClipChildrenMode(true);
                                mBinding.xbanner.setBannerData(bannerData);

                                //设置轮播的动画，默认情况下一屏多页左右的图片不会缩放，更改动画可以改变轮播的效果，
                                //Transformer还有很多效果，感兴趣的朋友可以自行尝试
                                mBinding.xbanner.setPageTransformer(Transformer.Scale);



                                //轮播图点击事件
                                mBinding.xbanner.setOnItemClickListener(new XBanner.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(XBanner banner, Object model,View view, int position) {
                                       // ToastUtils.showLong("点击了第"+position+"张图片"+"，名字为"+bannerData.get(position).name);

                                        Intent newIntent = new Intent(getContext(), FastSearchActivity.class);
                                        newIntent.putExtra("title", bannerData.get(position).name);
                                        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        getContext().startActivity(newIntent);


                                    }
                                });

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

    private boolean isPush(String text) {
        return !TextUtils.isEmpty(text) && Arrays.asList("smb", "http", "https", "thunder", "magnet", "ed2k", "mitv", "jianpian").contains(Uri.parse(text).getScheme());
    }


}