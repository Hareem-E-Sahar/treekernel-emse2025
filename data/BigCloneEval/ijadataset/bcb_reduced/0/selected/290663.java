package com.mlib.osgi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mlib.util.FileUtil;

/**
 * 服务组件管理容器
 * 
 * @author zitee@163.com
 * @创建时间 2009-09-17 11:20:53
 * @version 1.0
 */
public class Context {

    private static Context context = null;

    private String tempFilePath = "osgi_lib";

    private String filePath = "osgi_temp_lib";

    /**
	 * 服务类接口 实例对象
	 */
    private Map<String, Object> allService = new HashMap<String, Object>();

    private Context() {
    }

    /**
	 * 获取服务组件管理容器实例
	 * 
	 * @return
	 */
    public static Context getInstance() {
        if (context == null) context = new Context();
        return context;
    }

    /**
	 * 注册服务
	 * 
	 * @param clazz
	 * @param obj
	 */
    public void registerService(Class<?> clazz, Object obj) {
        allService.put(clazz.getName(), obj);
    }

    /**
	 * 注销服务
	 * 
	 * @param clazz
	 * @param obj
	 */
    public void unRegisterService(String className) {
        allService.remove(className);
    }

    /**
	 * 获取服务组件
	 * 
	 * @param clazz
	 * @return
	 */
    public Object getService(Class<?> clazz) {
        return allService.get(clazz.getName());
    }

    /**
	 * 返回当前所有服务组件信息 数据格式:服务组件接口,服务组件服务类
	 */
    public List<String> listServiceInfo() {
        List<String> olist = new ArrayList<String>();
        Object[] oarr = allService.keySet().toArray();
        Arrays.sort(oarr);
        for (int i = 0; i < oarr.length; i++) {
            olist.add(oarr[i] + "," + allService.get(oarr[i]).getClass().getName());
        }
        return olist;
    }

    /**
	 * 将给定的jar文件拷贝到运行目录和临时目录,然后返回运行时jar的路径
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
    private String initJarFilePath(String path) throws IOException {
        File sourceFile = new File(path);
        String targetFilePath = filePath + "/" + sourceFile.getName();
        String tempTargetFilePath = tempFilePath + "/" + sourceFile.getName() + "_" + UUID.randomUUID();
        File targetFile = new File(targetFilePath);
        File tempTargetFile = new File(tempTargetFilePath);
        FileUtil.copyFile(sourceFile, targetFile);
        FileUtil.copyFile(sourceFile, tempTargetFile);
        return tempTargetFilePath;
    }

    /**
	 * 注册一个jar包组件
	 * 
	 * @param jarFilePath
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
    public void registerJarComponent(String jarFilePath) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        String runtimeFilePath = initJarFilePath(jarFilePath);
        new ClassLoader(runtimeFilePath);
    }

    /**
	 * 设置临时jar文件存放地,当需要运行jar文件时,jar文件首先被拷贝到这个临时目录
	 * 
	 * @param tempFilePath
	 */
    public void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
        File fileDirectory = new File(tempFilePath);
        if (fileDirectory.isDirectory() && fileDirectory.exists()) {
            for (File file : fileDirectory.listFiles()) {
                file.delete();
            }
        }
    }

    /**
	 * 设置原始jar文件运行目录,jar文件首先被复制到此目录以供运行时使用
	 * 
	 * @param filePath
	 */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
