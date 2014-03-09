/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2014-2-28          mikan
 *
 */

package com.tianci.ad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.skyworth.framework.skysdk.android.SkySystemUtil;
import com.skyworth.framework.skysdk.android.SkySystemUtil.LINUX_CMD;
import com.tianci.ad.callback.AdResDownloadCallbackHandler;
import com.tianci.ad.callback.AdResMoveCallbackHandler;
import com.tianci.ad.interfaces.IAd;
import com.tianci.ad.interfaces.IAdRes.IAdResDownloadListener;
import com.tianci.ad.manager.AdDataManager;
import com.tianci.ad.manager.AdResManager;
import com.tianci.ad.util.AdLog;
import com.tianci.adservice.data.AdData;
import com.tianci.adservice.data.AdDataList;
import com.tianci.adservice.data.AdUIObject;
import com.tianci.adservice.defines.AdDefs.AdState;

/**
 * <p>
 * Description:Ad控制类
 * </p>
 * <p>
 * write something
 * </p>
 * 
 * @ClassName AdDirecctor
 * @author mikan
 * @date 2014-3-1
 * @version V1.0.0
 */
public class AdDirecctor implements IAd
{
    private static AdDirecctor instance = null;

    private AdDataList<AdData> adInfoDatas = null;
    private AdDataList<AdData> adActionDatas = null;

    private List<String> oldFileUrls = null;
    private List<String> newFileUrls = null;

    private IAdUpdataListener mAdUpdataListener = null;

    public static AdDirecctor getInstance()
    {
        if (instance == null)
        {
            instance = new AdDirecctor();
        }
        return instance;
    }

    private AdDirecctor()
    {

    }

    @Override
    public void setNewAdDataList(AdDataList<AdData> adInfoDatas, AdDataList<AdData> adActionDatas)
    {
        this.adInfoDatas = adInfoDatas;
        this.adActionDatas = adActionDatas;
    }

    @Override
    public void adUpdata(IAdUpdataListener updataListener)
    {
        mAdUpdataListener = updataListener;
        if (adInfoDatas != null && adInfoDatas.size() > 0 && adActionDatas != null
                && adActionDatas.size() > 0)
        {
            startDownloadAdRes(AdResDownloadCallbackHandler.getInstance(), adInfoDatas);
            updataListener.onAdUpdataStart();
            AdResMoveCallbackHandler.getInstance().setAdUpdateListener(mAdUpdataListener);
        }
        else
        {
            AdStateManager.getInstance().setAdState(AdState.AD_DISABLE);
            updateLocalAdResAndCache();
            AdStateManager.getInstance().setAdState(AdState.AD_ENABLE);
        }
    }

    @Override
    public void emptyAllAd()
    {
        AdLog.info("######### removeAllAdResource ########");

        AdStateManager.getInstance().setAdState(AdState.AD_DISABLE);
        AdDataManager.getInstance().cleanAllCache();
        AdResManager.getInstance().deleteAllResource();
    }

    private void startDownloadAdRes(IAdResDownloadListener downloadListener,
            AdDataList<AdData> adInfoDatas)
    {
        HashMap<String, List<String>> map = AdDataManager.getInstance().getNeedUpdateAdList(
                adInfoDatas);
        if (map != null)
        {
            oldFileUrls = map.get("old");
            newFileUrls = map.get("new");
            // AdResManager.getInstance().startAdResDownload(downloadListener,
            // newFileUrls);
            AdResManager.getInstance().downloadAdRes(downloadListener, newFileUrls);
        }
    }

    public void updateLocalAdResAndCache()
    {
        AdResManager.getInstance().deleteOldResourceFile(oldFileUrls);

        refreshCache();

        AdDataManager.getInstance().saveCache2Local();

        SkySystemUtil.execCmd(LINUX_CMD.LINUX_CMD_CHMOD, "-R 777 " + AdResManager.ROOT_AD_PATH);
        SkySystemUtil.execCmd(LINUX_CMD.LINUX_CMD_CHMOD, "777 " + AdResManager.BOOT_AD_FILE_PATH
                + AdResManager.BOOT_AD_FILE_NAME);
    }

    private void refreshCache()
    {
        AdLog.info("refreshCacheByWeb in adInfoDatas = " + adInfoDatas);
        if (adInfoDatas != null && adInfoDatas.size() > 0)
        {
            AdLog.info("adInfoDatas.size() = " + adInfoDatas.size());
            AdDataManager.getInstance().updateAdDataCache(adActionDatas);
            AdDataManager.getInstance().updateAdDataCache(adInfoDatas);
        }
        else
        {
            // removeAllAdResource();
            emptyAllAd();
        }
    }

    @Override
    public boolean loadLocalAdData()
    {
        return AdDataManager.getInstance().loadLocalAdData();
    }

    @Override
    public AdUIObject getAdUIObject(String actionName)
    {
        return AdDataManager.getInstance().getAdUIObject(actionName);
    }

    @Override
    public ArrayList<String> getFixAdMd5PathByFlag(String flag)
    {
        return AdDataManager.getInstance().getFixAdMd5PathByFlag(flag);
    }

    @Override
    public String getFixedAdResReadUseRights(String md5Path)
    {
        return AdResManager.getInstance().getFixedAdResReadUseRights(md5Path);
    }

    @Override
    public void releaseFixedAdResReadUseRights(String md5Path)
    {
        AdResManager.getInstance().releaseFixedAdResReadUseRights(md5Path);
    }

}
