package com.infinite.minio.common.util;

import java.io.IOException;
import java.util.Properties;

/**
 *  封装地址
 */
public class SysParamUtil {

    private static Properties prop;


    public SysParamUtil() {
    }

    static {
        reload();
    }

    public static boolean reload() {
        boolean flag = true;
        prop = new Properties();
        try{
            prop.load(SysParamUtil.class.getResourceAsStream("/application.properties"));
            flag = false;
        }catch (IOException e){
            e.printStackTrace();
        }
        return flag;
    }

    public static Properties getProp() {
        return prop;
    }
    public static String getProperties(String key) {
        return prop.getProperty(key);
    }

    public static void setProp(Properties prop) {
        SysParamUtil.prop = prop;
    }

}
