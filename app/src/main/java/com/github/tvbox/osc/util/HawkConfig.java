package com.github.tvbox.osc.util;

import android.util.Base64;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class HawkConfig {
    public static String APP_ID = "10001"; //应用ID2
    public static String Your_app_id = "98C9"; //TalkingData统计id  AndroidManifest.xml里面的也需要改
    public static String Your_channel_id = ""; //渠道随意
    public static String dt_ym = "0"; //是否开启动态域名 0开启 1关闭  开启填入动态域名  关闭填入域名 茶茶QQ205888578
    public static final String CONFIG_URL = "http://124.223.177.85:88/wbtvboxwb.json";//动态域名
    public static final String BASE_URL_ENC = "aHR0cH";//域名
    public static String API_KEY = "55885433f50db6915f3c94cf972c7002"; //接口密钥
    public static String MMM_MMM = new String(Base64.decode(HawkConfig.BASE_URL_ENC.getBytes(), Base64.DEFAULT));
    public static String zb_pzb = "茶茶QQ205888578"; //抓包 勿修改，修改不防抓包
    public static final String API_URL = "api_url";
    public static final String JSON_URL = "json_url";
    public static final String JSON_URL2 = "json_url2";//多线路
    public static final String LIVE_URL = "live_url";
    public static final String EPG_URL = "epg_url";
    public static final String SHOW_PREVIEW = "show_preview";
    public static final String SUBSCRIPTIONS = "api_history";
    public static final String LIVE_HISTORY = "live_history";
    public static final String EPG_HISTORY = "epg_history";
    public static final String HOME_API = "home_api";
    public static final String DEFAULT_PARSE = "parse_default";
    public static final String DEBUG_OPEN = "debug_open";
    public static final String PARSE_WEBVIEW = "parse_webview"; // true 系统 false xwalk
    public static final String IJK_CODEC = "ijk_codec";
    public static final String PLAY_TYPE = "play_type";//0 系统 1 ijk 2 exo 10 MXPlayer
    public static final String PLAY_RENDER = "play_render"; //0 texture 2
    public static final String PLAY_SCALE = "play_scale"; //0 texture 2
    public static final String PLAY_TIME_STEP = "play_time_step"; //0 texture 2
    public static final String DOH_URL = "doh_url";
    /**
     * 0 豆瓣热播 1 数据源推荐 2 关闭主页
     */
    public static final String HOME_REC = "home_rec";
    public static final String HISTORY_NUM = "history_num";
    public static final String LIVE_CHANNEL = "last_live_channel_name";
    public static final String LIVE_CHANNEL_REVERSE = "live_channel_reverse";
    public static final String LIVE_CROSS_GROUP = "live_cross_group";
    public static final String LIVE_CONNECT_TIMEOUT = "live_connect_timeout";
    public static final String LIVE_SHOW_NET_SPEED = "live_show_net_speed";
    public static final String LIVE_SHOW_TIME = "live_show_time";
    public static final String FAST_SEARCH_MODE = "fast_search_mode";
    public static final String SUBTITLE_TEXT_SIZE = "subtitle_text_size";
    public static final String SUBTITLE_TIME_DELAY = "subtitle_time_delay";
    public static final String SOURCES_FOR_SEARCH = "checked_sources_for_search";
    public static final String NOW_DATE = "now_date"; //当前日期
    public static final String REMOTE_TVBOX = "remote_tvbox_host";
    public static final String IJK_CACHE_PLAY = "ijk_cache_play";
    /**
     * 无痕浏览
     */
    public static final String PRIVATE_BROWSING = "private_browsing";
    /**
     * 主题,跟随系统0,浅1,深2
     */
    public static final String THEME_TAG = "theme_tag";
    /**
     * 后台播放模式 0 关闭,1 开启,2 画中画
     */
    public static final String BACKGROUND_PLAY_TYPE = "background_play_type";
    /**
     * TMDB请求token
     */
    public static final String TOKEN_TMDB = "token_tmdb";
}
