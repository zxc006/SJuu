package com.github.tvbox.osc.beanry;

import com.stx.xhb.androidx.entity.BaseBannerInfo;

import java.util.List;

public class AdvBean {
    public int code;
    public List<MsgDTO> msg;
    public int time;

    public static class MsgDTO implements BaseBannerInfo {
        public int code;
        public String name;
        public String extend;
        public String searchable;


        @Override
        public Object getXBannerUrl() {
            return extend;
        }

        @Override
        public String getXBannerTitle() {
            return name;
        }
        }
//
//    public int code;
//    public List<MsgDTO> msg;
//    public int time;
//
//    public static class MsgDTO {
//        public String name;
//        public String extend;
//        public String searchable;
//    }


}
