package com.hphtv.movielibrary.device;

import com.station.kit.util.Md5Utils;

import java.util.Random;

public class SignEncrypteTools {
    public static char[] TOKEN_CHARS = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    public static String getToken() {
        int len =TOKEN_CHARS.length;
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<4;i++){
            Random random=new Random();
            int pos=random.nextInt(len);
            sb.append(TOKEN_CHARS[pos]);
        }
        return sb.toString();
    }

    public static String getEncryptedSignature(String sn,String token,long timeStamp){
        String timeStampStr=String.valueOf(timeStamp);
        String devSn=sn;
        String tokenStr=token;
        StringBuffer rawSignBuffer=new StringBuffer();
        rawSignBuffer.append("timestamp=");
        rawSignBuffer.append(timeStampStr);
        rawSignBuffer.append("%devsn=");
        rawSignBuffer.append(devSn);
        rawSignBuffer.append("%token=");
        rawSignBuffer.append(tokenStr);
        return Md5Utils.digest(rawSignBuffer.toString());
    }


}
