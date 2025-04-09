package com.linktone.market.client.assist;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import com.linktone.market.client.R;
import com.linktone.market.client.bean.AppInfo;
import com.linktone.market.client.database.MarketDatabase;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mxf <a href="mailto:maxuefengs@gmail.com">mxf</a>
 *         11-8-26 ����1:50
 * @since version 1.0
 */
public class LocalAppInfoStatusUtil {

    private Context ct;

    private MarketDatabase mdb;

    public LocalAppInfoStatusUtil(Context context) {
        ct = context;
        mdb = MarketDatabase.getInstance(ct);
    }

    public void loadInstalledList(List<AppInfo> installedList) {
        installedList.clear();
        PackageManager pm = ct.getPackageManager();
        List<PackageInfo> packList = pm.getInstalledPackages(0);
        for (PackageInfo pack : packList) {
            if ((pack.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                AppInfo info = new AppInfo();
                info.setAppName(pack.applicationInfo.loadLabel(pm).toString());
                info.setPackageName(pack.packageName);
                info.setVersionCode(pack.versionCode);
                info.setVersionName(pack.versionName);
                info.setStatus(AppInfo.INSTALLED);
                info.setIconDrwable(pack.applicationInfo.loadIcon(pm));
                File f = new File(pack.applicationInfo.publicSourceDir);
                info.setTotalSize(f.length());
                installedList.add(info);
            }
        }
        Log.d(LOG_TAG, "installedList size" + installedList.size());
    }

    private static final String LOG_TAG = "Linktone-Market-LocalAppInfoStatusUtil";

    public void loadUpdatedList(List<AppInfo> installedList, List<AppInfo> updatedList) {
        Map<String, Integer> requestMap = new HashMap<String, Integer>();
        for (AppInfo info : installedList) {
            requestMap.put(info.getPackageName(), info.getVersionCode());
        }
        AppInfo info = new AppInfo();
        info.setId(2);
        info.setRating(4);
        info.setAppName("ˮ������");
        info.setPackageName("com.shuiguo");
        info.setVersionCode(1);
        info.setStatus(AppInfo.DOWNLOADING);
        info.setIconDrwable(ct.getResources().getDrawable(R.drawable.icon));
        info.setTotalSize(10701671);
        info.setUrl("http://211.99.200.92:8080/stat/pc.apk");
        updatedList.clear();
        updatedList.add(info);
        AppInfo info1 = new AppInfo();
        info1.setId(3);
        info1.setRating(3.4f);
        info1.setAppName("���");
        info1.setPackageName("com.miaobiao");
        info1.setVersionCode(1);
        info1.setStatus(AppInfo.DOWNLOADING);
        info1.setIconDrwable(ct.getResources().getDrawable(R.drawable.icon));
        info1.setTotalSize(2170007);
        info1.setUrl("http://211.99.200.92:8080/stat/mb.apk");
        updatedList.add(info1);
    }

    public void loadDownloadingList(List<AppInfo> downloadingList) {
        downloadingList.clear();
        downloadingList.addAll(mdb.getDownLoadingList());
    }

    public void loadDownloadedList(List<AppInfo> downloadedList) {
        downloadedList.clear();
        downloadedList.addAll(mdb.getDownLoadedList());
        for (AppInfo info : downloadedList) {
            showUninstallAPKIcon(info);
        }
    }

    private void showUninstallAPKIcon(AppInfo app) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = app.getLocalPath();
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(app.getLocalPath());
            valueArgs[1] = app.getLocalPath();
            valueArgs[2] = metrics;
            valueArgs[3] = 0;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            Class assetMagCls = Class.forName(PATH_AssetManager);
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = app.getLocalPath();
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = ct.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            CharSequence label = null;
            if (info.labelRes != 0) {
                label = res.getText(info.labelRes);
            }
            if (info.icon != 0) {
                Drawable icon = res.getDrawable(info.icon);
                app.setIconDrwable(icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
