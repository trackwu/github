package com.tiho.dlplugin.bean;

import java.util.ArrayList;
import java.util.List;

public class SdkInfoBean {
    public int count = 0;
    public List<Info> infos = new ArrayList<Info>();
    public static class Info{
        public String hsman = "";
        public String open = "";
        public String service = "";
    }
}
