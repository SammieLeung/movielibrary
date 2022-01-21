package com.hphtv.movielibrary.util;

/**
 * author: Sam Leung
 * date:  2022/1/21
 */
public class StringTools {
    public static String hideSmbAuthInfo(String smbPath){
        if(smbPath!=null&&smbPath.startsWith("smb://")){
            return smbPath.replaceFirst("smb://.*@","smb://");
        }
        return smbPath;
    }
}
