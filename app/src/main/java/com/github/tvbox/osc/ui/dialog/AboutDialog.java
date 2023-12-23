package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.beanry.InitBean;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.ToolUtils;
import com.lxj.xpopup.core.BottomPopupView;

import org.jetbrains.annotations.NotNull;

public class AboutDialog extends BottomPopupView {

    private InitBean initBean;
    private ImageView iv_AboutActivity_logo;

    public AboutDialog(@NonNull @NotNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_about;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        iv_AboutActivity_logo = findViewById(R.id.iv_AboutActivity_logo);

        initBean = MMkvUtils.loadInitBean("");

        String str =  "<font><samll>是xxx旗下互联网电视平台，独家提供xxx所有栏目以及自制高清、超清视频点播和直播内容，并为用户提供各类热门电影、电视剧、综艺、动漫等内容。<samll></font>";

        if (initBean != null) {
            if (ToolUtils.getIsEmpty(initBean.msg.appAbout)){
                if (initBean.msg.appAbout.contains("|")){
                    String[] aboutData = initBean.msg.appAbout.split("\\|");
                    str = "<font color='#ff9b26'><big>" + aboutData[0] + "</big></font>" + "<font><samll>" + aboutData[1] + "<samll></font>";
                }else{
                    str = "<font><samll>" + initBean.msg.appAbout + "<samll></font>";
                }
            }

            if (ToolUtils.getIsEmpty(initBean.msg.uiLogo)){
                Glide.with(this)
                        .load(initBean.msg.uiLogo)
                        .error(R.drawable.app_icon)
                        .into(iv_AboutActivity_logo);
            }
        } else {
           // user_fan_kui.setText("暂无");
        }

        TextView textView = findViewById(R.id.ab_text);
        textView.setText(Html.fromHtml(str));


    }
}