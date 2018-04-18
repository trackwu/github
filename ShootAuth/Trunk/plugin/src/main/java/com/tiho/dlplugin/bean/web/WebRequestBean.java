package com.tiho.dlplugin.bean.web;

import java.util.List;

/**
 * 请求web返回的Body
 * Created by Jerry on 2016/8/8.
 */
public class WebRequestBean {
    public List<WebBean> list;
    public int code;
    public String msg;
    public String session;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<WebBean> getList() {
        return list;
    }

    public void setList(List<WebBean> list) {
        this.list = list;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
